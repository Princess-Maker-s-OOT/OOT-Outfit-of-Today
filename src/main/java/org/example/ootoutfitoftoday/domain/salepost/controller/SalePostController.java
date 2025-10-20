package org.example.ootoutfitoftoday.domain.salepost.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostListResponse;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostSuccessCode;
import org.example.ootoutfitoftoday.domain.salepost.service.command.SalePostCommandService;
import org.example.ootoutfitoftoday.domain.salepost.service.query.SalePostQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<ApiResponse<SalePostCreateResponse>> createSalePost(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SalePostCreateRequest request
    ) {
        SalePostCreateResponse response = salePostCommandService.createSalePost(
                authUser.getUserId(),
                request,
                request.getImageUrls()
        );

        return ApiResponse.success(response, SalePostSuccessCode.SALE_POST_CREATED);
    }

    @GetMapping("/{salePostId}")
    public ResponseEntity<ApiResponse<SalePostDetailResponse>> getSalePostDetail(@PathVariable Long salePostId) {

        SalePostDetailResponse response = salePostQueryService.getSalePostDetail(salePostId);

        return ApiResponse.success(response, SalePostSuccessCode.SALE_POST_RETRIEVED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Slice<SalePostListResponse>>> getSalePosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) SaleStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<SalePostListResponse> salePosts = salePostQueryService.getSalePostList(
                categoryId,
                status,
                keyword,
                pageable
        );

        return ApiResponse.success(salePosts, SalePostSuccessCode.SALE_POST_RETRIEVED);
    }

    @PutMapping("{salePostId}")
    public ResponseEntity<ApiResponse<SalePostDetailResponse>> updateSalePost(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SalePostUpdateRequest request
    ) {
        SalePostDetailResponse response = salePostCommandService.updateSalePost(
                salePostId,
                authUser.getUserId(),
                request
        );

        return ApiResponse.success(response, SalePostSuccessCode.SALE_POST_UPDATED);
    }
}
