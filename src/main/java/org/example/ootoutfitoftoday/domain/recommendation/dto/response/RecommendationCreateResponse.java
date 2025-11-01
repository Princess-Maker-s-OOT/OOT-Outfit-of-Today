package org.example.ootoutfitoftoday.domain.recommendation.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.recommendation.status.RecommendationStatus;
import org.example.ootoutfitoftoday.domain.recommendation.type.RecommendationType;

import java.time.LocalDateTime;

/**
 * 추천 기록 생성 응답 DTO
 */
@Builder(access = AccessLevel.PRIVATE)
public record RecommendationCreateResponse(

        Long recommendationId,
        Long userId,
        Long clothesId,
        RecommendationType type,
        String reason,
        RecommendationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RecommendationCreateResponse from(Recommendation recommendation) {

        return RecommendationCreateResponse.builder()
                .recommendationId(recommendation.getId())
                .userId(recommendation.getUser().getId())
                .clothesId(recommendation.getClothes().getId())
                .type(recommendation.getType())
                .reason(recommendation.getReason())
                .status(recommendation.getStatus())
                .createdAt(recommendation.getCreatedAt())
                .updatedAt(recommendation.getUpdatedAt())
                .build();
    }
}