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
        return new ClosetSaveResponse(
                closet.getId(),
                closet.getUserId(),
                closet.getName(),
                closet.getDescription(),
                closet.getImageUrl(),
                closet.getIsPublic(),
                closet.getCreatedAt(),
                closet.getUpdatedAt()
        );
    }
}
