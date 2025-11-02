package org.example.ootoutfitoftoday.domain.recommendation.service.query;

import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecommendationQueryService {

    // 사용자 ID 기반으로 자신의 추천 기록을 페이징하여 조회
    Page<RecommendationGetMyResponse> getMyRecommendations(
            Long userId,
            Pageable pageable
    );
}