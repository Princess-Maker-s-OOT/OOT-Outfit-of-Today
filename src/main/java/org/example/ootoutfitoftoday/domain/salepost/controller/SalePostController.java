package org.example.ootoutfitoftoday.domain.salepost.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostSuccessCode;
import org.example.ootoutfitoftoday.domain.salepost.service.command.SalePostCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/sale-posts")
@RequiredArgsConstructor
public class SalePostController {

    private final SalePostCommandService salePostCommandService;

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

        return ApiResponse.success(response, SalePostSuccessCode.SALE_POSTS_CREATED);
    }
}
