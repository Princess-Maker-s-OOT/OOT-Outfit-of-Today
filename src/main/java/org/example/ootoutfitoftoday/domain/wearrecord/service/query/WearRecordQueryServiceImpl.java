package org.example.ootoutfitoftoday.domain.wearrecord.service.query;

import com.ootcommon.wearrecord.response.ClothesWearCount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordGetMyResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.entity.WearRecord;
import org.example.ootoutfitoftoday.domain.wearrecord.repository.WearRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
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
        log.debug("내 착용 기록 조회 시작 - 사용자 ID: {}, 페이지 정보: {}", userId, pageable);

        Page<WearRecord> wearRecords =
                wearRecordRepository.findMyWearRecordsWithClothes(
                        userId,
                        pageable
                );

        log.debug("내 착용 기록 조회 완료 - 조회된 기록 수: {}, 전체 기록 수: {}",
                wearRecords.getNumberOfElements(), wearRecords.getTotalElements());
        return wearRecords.map(WearRecordGetMyResponse::from);
    }

    @Override
    public List<ClothesWearCount> wornThisWeek(Long userId, LocalDate baseDate) {
        log.debug("이번 주 착용 빈도 조회 시작 - 사용자 ID: {}, 기준 날짜: {}", userId, baseDate);

        List<ClothesWearCount> result = wearRecordRepository.wornThisWeek(userId, baseDate);

        log.debug("이번 주 착용 빈도 조회 완료 - 조회된 옷 수: {}", result.size());
        return result;
    }

    @Override
    public List<ClothesWearCount> topWornClothes(Long userId) {
        log.debug("자주 입은 옷 조회 시작 - 사용자 ID: {}", userId);

        List<ClothesWearCount> result = wearRecordRepository.topWornClothes(userId);

        log.debug("자주 입은 옷 조회 완료 - 조회된 옷 수: {}", result.size());
        return result;
    }
}
