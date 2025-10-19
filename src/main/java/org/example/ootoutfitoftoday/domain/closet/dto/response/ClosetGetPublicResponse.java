package org.example.ootoutfitoftoday.domain.closet.dto.response;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;

import java.time.LocalDateTime;

public record ClosetGetPublicResponse(

        Long closetId,
        String name,
        String description,
        String imageUrl,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ClosetGetPublicResponse from(Closet closet) {

        return new ClosetGetPublicResponse(
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
