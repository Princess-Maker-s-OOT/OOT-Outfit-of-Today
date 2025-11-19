package org.example.ootoutfitoftoday.domain.recommendation.service.query;

import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecommendationQueryService {

    Page<RecommendationGetMyResponse> getMyRecommendations(Long userId, Pageable pageable);

    Recommendation findById(Long recommendationId);

    List<DonationCenterSearchResponse> searchDonationCentersFromRecommendation(
            Long recommendationId,
            Long userId,
            Integer radius,
            String keyword
    );
}