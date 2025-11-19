package org.example.ootoutfitoftoday.domain.wearrecord.service.query;

import com.ootcommon.wearrecord.response.ClothesWearCount;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordGetMyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface WearRecordQueryService {

    Page<WearRecordGetMyResponse> getMyWearRecords(Long userId, Pageable pageable);

    List<ClothesWearCount> wornThisWeek(Long userId, LocalDate baseDate);

    List<ClothesWearCount> topWornClothes(Long userId);
}