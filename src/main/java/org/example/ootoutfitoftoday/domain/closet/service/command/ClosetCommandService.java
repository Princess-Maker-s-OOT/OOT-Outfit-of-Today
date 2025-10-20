package org.example.ootoutfitoftoday.domain.closet.service.command;

import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetUpdateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetUpdateResponse;

public interface ClosetCommandService {

    // 옷장 수정
    ClosetUpdateResponse updateCloset(
            Long userId,
            Long closetId,
            ClosetUpdateRequest request
    );
}