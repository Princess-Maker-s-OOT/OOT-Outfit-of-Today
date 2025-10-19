package org.example.ootoutfitoftoday.domain.closet.dto.response;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;

import java.time.LocalDateTime;

public record ClosetGetResponse(

        Long closetId,
        String name,
        String description,
        String imageUrl,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ClosetGetResponse from(Closet closet) {

        return new ClosetGetResponse(
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