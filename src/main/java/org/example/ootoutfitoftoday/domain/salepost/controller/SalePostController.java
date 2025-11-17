package org.example.ootoutfitoftoday.domain.salepost.controller;

import com.ootcommon.salepost.enums.SaleStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SaleStatusUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.*;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostListResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostSummaryResponse;
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

@Slf4j
@Tag(name = "판매글 관리", description = "판매글 관련 API")
@RestController
@RequestMapping("/v1/sale-posts")
@RequiredArgsConstructor
public class SalePostController {

    private final SalePostCommandService salePostCommandService;
    private final SalePostQueryService salePostQueryService;

    @Operation(
            summary = "판매글 생성",
            description = "새로운 판매글을 등록합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
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
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
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
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
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
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,

            @AuthenticationPrincipal AuthUser authUser
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        log.info("[GET] /v1/sale-posts: categoryId={}, status={}, keyword={}, pageable={}", categoryId, status, keyword, pageable);

        Slice<SalePostListResponse> salePosts = salePostQueryService.getSalePostList(
                authUser.getUserId(),
                categoryId,
                status,
                keyword,
                pageable
        );

        return Response.success(salePosts, SalePostSuccessCode.SALE_POSTS_RETRIEVED);
    }

    @Operation(
            summary = "판매글 수정",
            description = "기존 판매글을 수정합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
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

    @Operation(
            summary = "판매글 삭제",
            description = "판매글을 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
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

    @Operation(
            summary = "판매글 상태 변경",
            description = "판매글의 판매 상태를 변경합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "변경 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
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

    @Operation(
            summary = "내 판매글 조회",
            description = "내가 작성한 판매글들을 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
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

    @Operation(
            summary = "비회원 판매글 전체 조회",
            description = "카테고리/상태/키워드로 필터링 된 전체 판매글을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @GetMapping("/public")
    public ResponseEntity<Response<Slice<SalePostPublicListResponse>>> getNotAuthSalePosts(
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

        Slice<SalePostPublicListResponse> salePosts = salePostQueryService.getNotAuthSalePostList(
                categoryId,
                status,
                keyword,
                pageable
        );

        return Response.success(salePosts, SalePostSuccessCode.SALE_POSTS_RETRIEVED);
    }
}
