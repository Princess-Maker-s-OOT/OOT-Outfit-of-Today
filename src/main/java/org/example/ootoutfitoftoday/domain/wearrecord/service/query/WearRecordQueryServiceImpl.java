package org.example.ootoutfitoftoday.domain.wearrecord.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordGetMyResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.entity.WearRecord;
import org.example.ootoutfitoftoday.domain.wearrecord.repository.WearRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WearRecordQueryServiceImpl implements WearRecordQueryService {

    private final WearRecordRepository wearRecordRepository;

    // 내 착용 기록 리스트 조회
    @Override
    public Page<WearRecordGetMyResponse> getMyWearRecords(
            Long userId,
            Pageable pageable
    ) {

        Page<WearRecord> wearRecords =
                wearRecordRepository.findMyWearRecordsWithClothes(
                        userId,
                        pageable
                );

        return wearRecords.map(WearRecordGetMyResponse::from);
    }
}
