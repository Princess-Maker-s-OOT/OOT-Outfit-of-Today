package org.example.ootoutfitoftoday.domain.wearrecord.service.query;

import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordGetMyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WearRecordQueryService {

    // 사용자 ID 기반으로 자신의 착용 기록을 페이징하여 조회
    Page<WearRecordGetMyResponse> getMyWearRecords(
            Long userId,
            Pageable pageable
    );
}
