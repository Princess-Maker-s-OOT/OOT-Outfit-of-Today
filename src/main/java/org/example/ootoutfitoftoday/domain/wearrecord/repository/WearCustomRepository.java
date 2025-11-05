package org.example.ootoutfitoftoday.domain.wearrecord.repository;

import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.ClothesWearCount;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.NotWornOverPeriod;

import java.time.LocalDate;
import java.util.List;

public interface WearCustomRepository {

    // 이번 주 착용 빈도 높은 옷
    List<ClothesWearCount> wornThisWeek(Long userId, LocalDate baseDate);

    // 자주 입은 옷 (전체 기간)
    List<ClothesWearCount> topWornClothes(Long userId);

    // 옷 미착용 기간
    List<NotWornOverPeriod> notWornOverPeriod(Long userId);
}
