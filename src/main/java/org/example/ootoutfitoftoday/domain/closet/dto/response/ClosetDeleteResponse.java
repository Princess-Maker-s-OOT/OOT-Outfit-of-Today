package org.example.ootoutfitoftoday.domain.closet.dto.response;

import java.time.LocalDateTime;

public record ClosetDeleteResponse(

        Long closetId,
        LocalDateTime deletedAt
) {
    public static ClosetDeleteResponse of(
            Long closetId,
            LocalDateTime deletedAt
    ) {

        return new ClosetDeleteResponse(closetId, deletedAt);
    }
}
