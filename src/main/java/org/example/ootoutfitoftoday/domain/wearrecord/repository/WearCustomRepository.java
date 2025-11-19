package org.example.ootoutfitoftoday.domain.wearrecord.repository;

import com.ootcommon.wearrecord.response.ClothesWearCount;

import java.time.LocalDate;
import java.util.List;

public interface WearCustomRepository {

    List<ClothesWearCount> wornThisWeek(Long userId, LocalDate baseDate);

    List<ClothesWearCount> topWornClothes(Long userId);
}