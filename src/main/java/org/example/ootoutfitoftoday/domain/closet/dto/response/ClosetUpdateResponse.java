package org.example.ootoutfitoftoday.domain.closet.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;

import java.time.LocalDateTime;

/**
 * 옷장 수정 응답 DTO
 */
@Builder(access = AccessLevel.PRIVATE)
public record ClosetUpdateResponse(
        Long closetId,
        String name,
        String description,
        String imageUrl,
        Boolean isPublic,
        LocalDateTime updatedAt
) {
    public static ClosetUpdateResponse from(Closet closet) {

        String imageUrl = closet.getImageUrl();

        return ClosetUpdateResponse.builder()
                .closetId(closet.getId())
                .name(closet.getName())
                .description(closet.getDescription())
                .imageUrl(imageUrl)
                .isPublic(closet.getIsPublic())
                .updatedAt(closet.getUpdatedAt())
                .build();
    }
}