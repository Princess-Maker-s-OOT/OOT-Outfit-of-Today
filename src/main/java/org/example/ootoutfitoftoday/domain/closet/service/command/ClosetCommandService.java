package org.example.ootoutfitoftoday.domain.closet.service.command;

import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetCreateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetUpdateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetCreateResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetDeleteResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetUpdateResponse;

public interface ClosetCommandService {

    ClosetCreateResponse createCloset(Long userId, ClosetCreateRequest request);

    ClosetUpdateResponse updateCloset(
            Long userId,
            Long closetId,
            ClosetUpdateRequest request
    );

    ClosetDeleteResponse deleteCloset(Long userId, Long closetId);
}