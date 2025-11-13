package org.example.ootoutfitoftoday.domain.recommendation.batch.dto;


import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record RecommendationBatchResult(

        Long userId,
        List<Recommendation> recommendations,
        boolean success,
        String errorMessage
) {
    /**
     * 성공 케이스 생성
     */
    public static RecommendationBatchResult success(Long userId, List<Recommendation> recommendations) {

        return RecommendationBatchResult.builder()
                .userId(userId)
                .recommendations(recommendations)
                .success(true)
                .build();
    }

    /**
     * 실패 케이스 생성
     */
    public static RecommendationBatchResult failure(Long userId, String errorMessage) {

        return RecommendationBatchResult.builder()
                .userId(userId)
                .recommendations(List.of())
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 생성된 추천 개수 반환
     */
    public int getRecommendationCount() {

        return recommendations != null ? recommendations.size() : 0;
    }
}
