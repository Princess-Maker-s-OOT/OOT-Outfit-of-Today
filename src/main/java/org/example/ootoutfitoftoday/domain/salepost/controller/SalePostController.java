package org.example.ootoutfitoftoday.domain.salepost.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SaleStatusUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostListResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostSummaryResponse;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostSuccessCode;
import org.example.ootoutfitoftoday.domain.salepost.service.command.SalePostCommandService;
import org.example.ootoutfitoftoday.domain.salepost.service.query.SalePostQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/sale-posts")
@RequiredArgsConstructor
public class SalePostController {

    private final SalePostCommandService salePostCommandService;
    private final SalePostQueryService salePostQueryService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "판매글 생성",
            description = "새로운 판매글을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "판매글이 성공적으로 생성되었습니다."),
                    @ApiResponse(responseCode = "400", description = "카테고리를 찾을 수 없습니다."),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 사용자입니다."),
                    @ApiResponse(responseCode = "404", description = "가격은 0보다 커야 합니다."),
                    @ApiResponse(responseCode = "404", description = "이미지를 최소 1개 이상 등록해주세요."),
                    @ApiResponse(responseCode = "404", description = "일부 이미지가 제대로 업로드되지 않았습니다."),
                    @ApiResponse(responseCode = "404", description = "중복된 이미지입니다.")
            }
    )
    @PostMapping
    public ResponseEntity<Response<SalePostCreateResponse>> createSalePost(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SalePostCreateRequest request
    ) {
        SalePostCreateResponse response = salePostCommandService.createSalePost(
                authUser.getUserId(),
                request,
                request.getImageUrls()
        );

        return Response.success(response, SalePostSuccessCode.SALE_POST_CREATED);
    }

    @Operation(
            summary = "판매글 상세 조회",
            description = "판매글의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "판매글이 성공적으로 조회되었습니다."),
                    @ApiResponse(responseCode = "400", description = "판매글을 찾을 수 없습니다.")
            }
    )
    @GetMapping("/{salePostId}")
    public ResponseEntity<Response<SalePostDetailResponse>> getSalePostDetail(@PathVariable Long salePostId) {

        SalePostDetailResponse response = salePostQueryService.getSalePostDetail(salePostId);

        return Response.success(response, SalePostSuccessCode.SALE_POST_RETRIEVED);
    }

    @Operation(
            summary = "판매글 전체 조회",
            description = "카테고리/상태/키워드로 필터링 된 전체 판매글을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "판매글이 성공적으로 조회되었습니다."),
            }
    )
    @GetMapping
    public ResponseEntity<Response<Slice<SalePostListResponse>>> getSalePosts(
            @Parameter(description = "카테고리 ID")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "판매 상태 (SELLING, RESERVED, SOLD_OUT)")
            @RequestParam(required = false) SaleStatus status,

            @Parameter(description = "검색어 (제목/내용 검색)")
            @RequestParam(required = false) String keyword,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        Slice<SalePostListResponse> salePosts = salePostQueryService.getSalePostList(
                categoryId,
                status,
                keyword,
                pageable
        );

        return Response.success(salePosts, SalePostSuccessCode.SALE_POSTS_RETRIEVED);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "판매글 수정",
            description = "기존 판매글을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "판매글이 수정되었습니다."),
                    @ApiResponse(responseCode = "400", description = "판매글을 찾을 수 없습니다."),
                    @ApiResponse(responseCode = "403", description = "해당 판매글에 대한 권한이 없습니다."),
                    @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없습니다."),
                    @ApiResponse(responseCode = "404", description = "이미지를 최소 1개 이상 등록해주세요."),
                    @ApiResponse(responseCode = "404", description = "일부 이미지가 제대로 업로드되지 않았습니다."),
                    @ApiResponse(responseCode = "404", description = "중복된 이미지입니다.")
            }
    )
    @PutMapping("/{salePostId}")
    public ResponseEntity<Response<SalePostDetailResponse>> updateSalePost(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SalePostUpdateRequest request
    ) {
        SalePostDetailResponse response = salePostCommandService.updateSalePost(
                salePostId,
                authUser.getUserId(),
                request
        );

        return Response.success(response, SalePostSuccessCode.SALE_POST_UPDATED);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "판매글 삭제",
            description = "판매글을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "판매글이 삭제되었습니다."),
                    @ApiResponse(responseCode = "400", description = "판매글을 찾을 수 없습니다."),
                    @ApiResponse(responseCode = "403", description = "해당 판매글에 대한 권한이 없습니다."),
                    @ApiResponse(responseCode = "404", description = "예약 중인 판매글은 삭제할 수 없습니다.")
            }
    )
    @DeleteMapping("/{salePostId}")
    public ResponseEntity<Response<Void>> deleteSalePost(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        salePostCommandService.deleteSalePost(salePostId, authUser.getUserId());

        return Response.success(null, SalePostSuccessCode.SALE_POST_DELETED);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "판매글 상태 변경",
            description = "판매글의 판매 상태를 변경합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "판매글의 상태가 수정되었습니다."),
                    @ApiResponse(responseCode = "400", description = "판매글을 찾을 수 없습니다."),
                    @ApiResponse(responseCode = "403", description = "해당 판매글에 대한 권한이 없습니다."),

            }
    )
    @PatchMapping("/{salePostId}/status")
    public ResponseEntity<Response<SalePostDetailResponse>> updateSaleStatus(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SaleStatusUpdateRequest request
    ) {
        SalePostDetailResponse response = salePostCommandService.updateSaleStatus(
                salePostId,
                authUser.getUserId(),
                request.getStatus()
        );

        return Response.success(response, SalePostSuccessCode.SALE_POST_STATUS_UPDATED);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "내 판매글 조회",
            description = "내가 작성한 판매글들을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "판매글이 성공적으로 조회되었습니다."),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 사용자입니다.")
            }
    )
    @GetMapping("/my")
    public ResponseEntity<Response<Slice<SalePostSummaryResponse>>> getMySalePosts(
            @AuthenticationPrincipal AuthUser authUser,

            @Parameter(description = "판매 상태 (SELLING, RESERVED, SOLD_OUT)")
            @RequestParam(required = false) SaleStatus status,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        Slice<SalePostSummaryResponse> response = salePostQueryService.findMySalePosts(
                authUser.getUserId(),
                status,
                pageable
        );

        return Response.success(response, SalePostSuccessCode.SALE_POSTS_RETRIEVED);
    }
}
