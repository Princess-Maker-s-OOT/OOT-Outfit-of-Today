package org.example.ootoutfitoftoday.domain.salepost.service.command;

import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;

import java.math.BigDecimal;
import java.util.List;

public interface SalePostCommandService {

    SalePostCreateResponse createSalePost(
            Long userId,
            SalePostCreateRequest request,
            List<String> imageUrls
    );

    SalePostCreateResponse createSalePostFromRecommendation(
            Recommendation recommendation,
            Long categoryId,
            String title,
            String content,
            BigDecimal price,
            String tradeAddress,
            BigDecimal tradeLatitude,
            BigDecimal tradeLongitude,
            List<String> imageUrls
    );

    SalePostDetailResponse updateSalePost(
            Long salePostId,
            Long userId,
            SalePostUpdateRequest request
    );

    void deleteSalePost(Long salePostId, Long userId);

    SalePostDetailResponse updateSaleStatus(
            Long salePostId,
            Long userId,
            SaleStatus newStatus
    );
}
