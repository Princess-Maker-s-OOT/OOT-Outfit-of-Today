package org.example.ootoutfitoftoday.domain.wearrecord.service.command;

import org.example.ootoutfitoftoday.domain.wearrecord.dto.request.WearRecordCreateRequest;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordCreateResponse;

public interface WearRecordCommandService {

    WearRecordCreateResponse createWearRecord(Long userId, WearRecordCreateRequest request);
}