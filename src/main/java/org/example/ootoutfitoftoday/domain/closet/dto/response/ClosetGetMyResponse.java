package org.example.ootoutfitoftoday.domain.closet.dto.response;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;

import java.time.LocalDateTime;

public record ClosetGetMyResponse(

        Long closetId,
        String name,
        String description,
        String imageUrl,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ClosetGetMyResponse from(Closet closet) {

        return new ClosetGetMyResponse(
                closet.getId(),
                closet.getName(),
                closet.getDescription(),
                closet.getImageUrl(),
                closet.getIsPublic(),
                closet.getCreatedAt(),
                closet.getUpdatedAt()
        );
    }
}