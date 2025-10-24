package org.example.ootoutfitoftoday.domain.dashboard.service.query.user;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.DashboardUserSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardUserQueryServiceImpl implements DashboardUserQueryService {

    private final ClothesQueryService clothesQueryService;

    @Override
    public DashboardUserSummaryResponse getUserDashboardSummary(Long userId) {

        int totalCount = clothesQueryService.countAllClothesByUserIdAndIsDeletedFalse(userId);

        List<CategoryStat> categoryStats = clothesQueryService.countUserTopCategoryStats(userId);

        return new DashboardUserSummaryResponse(totalCount,categoryStats);
    }
}
