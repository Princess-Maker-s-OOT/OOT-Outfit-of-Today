package org.example.ootoutfitoftoday.domain.dashboard.service.query.user;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.DashboardUserSummaryResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.DashboardUserWearStatisticsResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.ClothesWearCount;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.NotWornOverPeriod;
import org.example.ootoutfitoftoday.domain.wearrecord.service.query.WearRecordQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardUserQueryServiceImpl implements DashboardUserQueryService {

    private final ClothesQueryService clothesQueryService;
    private final WearRecordQueryService wearRecordQueryService;

    @Override
    public DashboardUserSummaryResponse getUserDashboardSummary(Long userId) {

        int totalCount = clothesQueryService.countAllClothesByUserIdAndIsDeletedFalse(userId);

        List<CategoryStat> categoryStats = clothesQueryService.countUserTopCategoryStats(userId);

        return new DashboardUserSummaryResponse(totalCount, categoryStats);
    }

    @Override
    public DashboardUserWearStatisticsResponse getUserWearStatistics(Long userId, LocalDate baseDate) {

        List<ClothesWearCount> wornThisWeek = wearRecordQueryService.wornThisWeek(userId, baseDate);

        List<ClothesWearCount> topWornClothes = wearRecordQueryService.topWornClothes(userId);

        List<ClothesWearCount> leastWornClothes = clothesQueryService.leastWornClothes(userId);

        List<NotWornOverPeriod> notWornOverPeriod = wearRecordQueryService.notWornOverPeriod(userId);

        return new DashboardUserWearStatisticsResponse(
                wornThisWeek,
                topWornClothes,
                leastWornClothes,
                notWornOverPeriod
        );
    }
}
