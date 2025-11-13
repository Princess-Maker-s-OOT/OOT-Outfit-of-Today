package org.example.ootoutfitoftoday.domain.recommendation.service.query;

import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecommendationQueryService {

    // 사용자 ID 기반으로 자신의 추천 기록을 페이징하여 조회
    Page<RecommendationGetMyResponse> getMyRecommendations(
            Long userId,
            Pageable pageable
    );

    // 추천 ID로 추천 조회
    Recommendation findById(Long recommendationId);

    // 추천으로부터 기부처 검색
    List<DonationCenterSearchResponse> searchDonationCentersFromRecommendation(
            Long recommendationId,
            Long userId,
            Integer radius,
            String keyword
    );
}