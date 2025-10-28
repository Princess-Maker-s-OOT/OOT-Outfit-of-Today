package org.example.ootoutfitoftoday.domain.wearrecord.service.command;

import org.example.ootoutfitoftoday.domain.wearrecord.dto.request.WearRecordCreateRequest;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordCreateResponse;

public interface WearRecordCommandService {

    // 착용 기록을 생성하고, 해당 옷의 마지막 착용 일시를 업데이트
    WearRecordCreateResponse createWearRecord(
            Long userId,
            WearRecordCreateRequest request
    );
}