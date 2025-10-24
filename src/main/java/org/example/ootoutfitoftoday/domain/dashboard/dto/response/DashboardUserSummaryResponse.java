package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardUserSummaryResponse {

    private final int totalClothes; // 등록된 옷 전체 수
//    private final int totalWearCount; 총 착용 횟수
//    private final List<NotWornOverPeriodResponse> notWornOverPeriodResponse; 미착용 기간
    private final List<CategoryStat> categoryStat; // 카테고리별 옷 개수 limit 10
//    private final List<TopWornClothes> topWornClothes; 자주 입은 옷
//    private final List<LeastWornClothes> leastWornClothes; 자주 입지 않은 옷
}
