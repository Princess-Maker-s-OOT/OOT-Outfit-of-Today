package org.example.ootoutfitoftoday.domain.recommendation.dto.response;

import com.ootcommon.recommendation.status.RecommendationStatus;
import com.ootcommon.recommendation.type.RecommendationType;
import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record RecommendationGetMyResponse(

        Long recommendationId,
        Long userId,
        Long clothesId,
        String clothesName,
        String clothesImageUrl,
        RecommendationType type,
        String reason,
        RecommendationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RecommendationGetMyResponse from(Recommendation recommendation) {
        Clothes clothes = recommendation.getClothes();

        String name = clothes.getDescription();

        String imageUrl = null;

        if (clothes.getImages() != null && !clothes.getImages().isEmpty()) {
            imageUrl = clothes.getImages().stream()
                    .filter(image -> Boolean.TRUE.equals(image.getIsMain()))
                    .findFirst()
                    .map(mainImage -> mainImage.getImage().getUrl())
                    .orElse(clothes.getImages().get(0).getImage().getUrl());
        }

        return RecommendationGetMyResponse.builder()
                .recommendationId(recommendation.getId())
                .userId(recommendation.getUser().getId())
                .clothesId(clothes.getId())
                .clothesName(name)
                .clothesImageUrl(imageUrl)
                .type(recommendation.getType())
                .reason(recommendation.getReason())
                .status(recommendation.getStatus())
                .createdAt(recommendation.getCreatedAt())
                .updatedAt(recommendation.getUpdatedAt())
                .build();
    }
}