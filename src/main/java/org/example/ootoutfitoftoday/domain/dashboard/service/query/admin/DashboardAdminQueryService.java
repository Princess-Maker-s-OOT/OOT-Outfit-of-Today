package org.example.ootoutfitoftoday.domain.dashboard.service.query.admin;

import com.ootcommon.dashboard.response.AdminClothesStatisticsResponse;
import com.ootcommon.dashboard.response.AdminSalePostStatisticsResponse;
import com.ootcommon.dashboard.response.AdminTopCategoryStatisticsResponse;
import com.ootcommon.dashboard.response.AdminUserStatisticsResponse;

import java.time.LocalDate;

public interface DashboardAdminQueryService {

    AdminUserStatisticsResponse adminUserStatistics(LocalDate baseDate);

    AdminClothesStatisticsResponse adminClothesStatistics();

    AdminSalePostStatisticsResponse adminSalePostStatistics(LocalDate baseDate);

    AdminTopCategoryStatisticsResponse adminTopCategoryStatistics();
}
