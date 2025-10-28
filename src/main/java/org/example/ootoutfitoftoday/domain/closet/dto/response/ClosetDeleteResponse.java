package org.example.ootoutfitoftoday.domain.closet.dto.response;

import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 옷장 삭제 응답 DTO
 */
@Builder(access = AccessLevel.PRIVATE)
public record ClosetDeleteResponse(

        Long closetId,
        LocalDateTime deletedAt
) {
    public static ClosetDeleteResponse of(
            Long closetId,
            LocalDateTime deletedAt
    ) {

        return ClosetDeleteResponse.builder()
                .closetId(closetId)
                .deletedAt(deletedAt)
                .build();
    }
}