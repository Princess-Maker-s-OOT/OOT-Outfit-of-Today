package org.example.ootoutfitoftoday.domain.closet.service.command;

import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetSaveRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetSaveResponse;

public interface ClosetCommandService {

    // 옷장 등록
    ClosetSaveResponse createCloset(ClosetSaveRequest closetSaveRequest);
}