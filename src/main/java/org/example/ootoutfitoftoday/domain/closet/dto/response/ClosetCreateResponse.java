package org.example.ootoutfitoftoday.domain.closet.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;

import java.time.LocalDateTime;

/**
 * 옷장 등록 응답 DTO
 */
@Builder(access = AccessLevel.PRIVATE)
public record ClosetCreateResponse(

        Long closetId,
        Long userId,
        String name,
        String description,
        String imageUrl,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ClosetCreateResponse from(Closet closet) {

        String imageUrl = closet.getImageUrl();

        return ClosetCreateResponse.builder()
                .closetId(closet.getId())
                .userId(closet.getUserId())
                .name(closet.getName())
                .description(closet.getDescription())
                .imageUrl(imageUrl)
                .isPublic(closet.getIsPublic())
                .createdAt(closet.getCreatedAt())
                .updatedAt(closet.getUpdatedAt())
                .build();
    }
}