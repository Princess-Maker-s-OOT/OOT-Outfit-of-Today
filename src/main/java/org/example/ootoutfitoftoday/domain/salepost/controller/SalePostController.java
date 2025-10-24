package org.example.ootoutfitoftoday.domain.salepost.controller;

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

    @GetMapping("/{salePostId}")
    public ResponseEntity<Response<SalePostDetailResponse>> getSalePostDetail(@PathVariable Long salePostId) {

        SalePostDetailResponse response = salePostQueryService.getSalePostDetail(salePostId);

        return Response.success(response, SalePostSuccessCode.SALE_POST_RETRIEVED);
    }

    @GetMapping
    public ResponseEntity<Response<Slice<SalePostListResponse>>> getSalePosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) SaleStatus status,
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

    @DeleteMapping("/{salePostId}")
    public ResponseEntity<Response<Void>> deleteSalePost(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        salePostCommandService.deleteSalePost(salePostId, authUser.getUserId());

        return Response.success(null, SalePostSuccessCode.SALE_POST_DELETED);
    }

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

    @GetMapping("/my")
    public ResponseEntity<Response<Slice<SalePostSummaryResponse>>> getMySalePosts(
            @AuthenticationPrincipal AuthUser authUser,
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

        return Response.success(response, SalePostSuccessCode.SALE_POST_RETRIEVED);
    }
}
