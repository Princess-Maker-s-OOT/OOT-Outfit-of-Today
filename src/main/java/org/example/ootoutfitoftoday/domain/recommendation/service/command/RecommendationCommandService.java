package org.example.ootoutfitoftoday.domain.recommendation.service.command;

import org.example.ootoutfitoftoday.domain.recommendation.dto.request.RecommendationSalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationCreateResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;

import java.util.List;

public interface RecommendationCommandService {

    List<RecommendationCreateResponse> generateRecommendations(Long userId);

    List<Recommendation> createRecommendationsForBatch(Long userId);

    SalePostCreateResponse createSalePostFromRecommendation(
            Long recommendationId,
            Long userId,
            RecommendationSalePostCreateRequest request
    );
}