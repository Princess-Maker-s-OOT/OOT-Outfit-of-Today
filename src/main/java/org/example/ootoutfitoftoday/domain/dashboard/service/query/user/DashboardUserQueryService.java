package org.example.ootoutfitoftoday.domain.dashboard.service.query.user;

import org.example.ootoutfitoftoday.domain.dashboard.dto.response.DashboardUserSummaryResponse;

public interface DashboardUserQueryService {

    DashboardUserSummaryResponse getUserDashboardSummary(Long userId);
}
