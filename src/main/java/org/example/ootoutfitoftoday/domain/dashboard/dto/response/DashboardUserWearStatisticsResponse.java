package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.ClothesWearCount;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.NotWornOverPeriod;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardUserWearStatisticsResponse {

    private final List<ClothesWearCount> wornThisWeek;// 이번주 착용 빈도 높은 옷
    private final List<ClothesWearCount> topWornClothes; // 자주 입은 옷
    private final List<ClothesWearCount> leastWornClothes; // 자주 입지 않은 옷
    private final List<NotWornOverPeriod> notWornOverPeriod; // 옷 미착용 기간
}
