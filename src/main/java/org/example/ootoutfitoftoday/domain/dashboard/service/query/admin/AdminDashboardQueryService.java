package org.example.ootoutfitoftoday.domain.dashboard.service.query.admin;

import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminClothesStatisticsResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminSalePostStatisticsResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminUserStatisticsResponse;

import java.time.LocalDate;

public interface AdminDashboardQueryService {

    AdminUserStatisticsResponse adminUserStatistics(LocalDate baseDate);

    AdminClothesStatisticsResponse adminClothesStatistics();

    AdminSalePostStatisticsResponse adminSalePostStatistics(LocalDate baseDate);
}
