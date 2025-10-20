package org.example.ootoutfitoftoday.domain.closet.service.command;

import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetSaveRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetUpdateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetDeleteResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetSaveResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetUpdateResponse;

public interface ClosetCommandService {

    // 옷장 등록
    ClosetSaveResponse createCloset(
            Long userId,
            ClosetSaveRequest request
    );

    // 옷장 수정
    ClosetUpdateResponse updateCloset(
            Long userId,
            Long closetId,
            ClosetUpdateRequest request
    );

    // 옷장 삭제
    ClosetDeleteResponse deleteCloset(
            Long userId,
            Long closetId
    );
}