package org.example.ootoutfitoftoday.domain.dashboard.service.query.user;

import org.example.ootoutfitoftoday.domain.dashboard.dto.response.DashboardUserSummaryResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.DashboardUserWearStatisticsResponse;

import java.time.LocalDate;

public interface DashboardUserQueryService {

    DashboardUserSummaryResponse getUserDashboardSummary(Long userId);

    DashboardUserWearStatisticsResponse getUserWearStatistics(Long userId, LocalDate baseDate);
}
