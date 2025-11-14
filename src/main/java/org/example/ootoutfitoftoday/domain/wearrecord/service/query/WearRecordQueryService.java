package org.example.ootoutfitoftoday.domain.wearrecord.service.query;

import com.ootcommon.wearrecord.response.ClothesWearCount;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordGetMyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface WearRecordQueryService {

    // 사용자 ID 기반으로 자신의 착용 기록을 페이징하여 조회
    Page<WearRecordGetMyResponse> getMyWearRecords(
            Long userId,
            Pageable pageable
    );

    // 이번 주 착용 빈도 높은 옷
    List<ClothesWearCount> wornThisWeek(Long userId, LocalDate baseDate);

    // 자주 입은 옷 (전체 기간)
    List<ClothesWearCount> topWornClothes(Long userId);
}
