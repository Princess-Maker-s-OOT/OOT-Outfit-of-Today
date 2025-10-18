package org.example.ootoutfitoftoday.domain.closet.dto.response;

import lombok.Builder;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;

import java.time.LocalDateTime;

@Builder
public record ClosetSaveResponse(
        Long closetId,
        Long userId,
        String name,
        String description,
        String imageUrl,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ClosetSaveResponse from(Closet closet) {
        return ClosetSaveResponse.builder()
                .closetId(closet.getId())
                .userId(closet.getUserId())
                .name(closet.getName())
                .description(closet.getDescription())
                .imageUrl(closet.getImageUrl())
                .isPublic(closet.getIsPublic())
                .createdAt(closet.getCreatedAt())
                .updatedAt(closet.getUpdatedAt())
                .build();
    }
}
