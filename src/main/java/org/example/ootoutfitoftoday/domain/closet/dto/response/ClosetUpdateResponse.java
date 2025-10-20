package org.example.ootoutfitoftoday.domain.closet.dto.response;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;

import java.time.LocalDateTime;

public record ClosetUpdateResponse(
        Long closetId,
        String name,
        String description,
        String imageUrl,
        Boolean isPublic,
        LocalDateTime updatedAt
) {
    public static ClosetUpdateResponse from(Closet closet) {

        return new ClosetUpdateResponse(
                closet.getId(),
                closet.getName(),
                closet.getDescription(),
                closet.getImageUrl(),
                closet.getIsPublic(),
                closet.getUpdatedAt()
        );
    }
}