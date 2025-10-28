package org.example.ootoutfitoftoday.domain.closet.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;

import java.time.LocalDateTime;

/**
 * 옷장 공개 조회 응답 DTO
 */
@Builder(access = AccessLevel.PRIVATE)
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

        String imageUrl = closet.getImageUrl();

        return ClosetGetPublicResponse.builder()
                .closetId(closet.getId())
                .name(closet.getName())
                .description(closet.getDescription())
                .imageUrl(imageUrl)
                .isPublic(closet.getIsPublic())
                .createdAt(closet.getCreatedAt())
                .updatedAt(closet.getUpdatedAt())
                .build();
    }
}