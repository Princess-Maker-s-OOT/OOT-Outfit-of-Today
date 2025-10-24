package org.example.ootoutfitoftoday.domain.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminClothesStatisticsResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminSalePostStatisticsResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminTopCategoryStatisticsResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminUserStatisticsResponse;
import org.example.ootoutfitoftoday.domain.dashboard.exception.DashboardSuccessCode;
import org.example.ootoutfitoftoday.domain.dashboard.service.query.admin.AdminDashboardQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/v1/dashboards")
public class AdminDashboardController {

    private final AdminDashboardQueryService adminDashboardQueryService;

    @GetMapping("/users/statistics")
    public ResponseEntity<Response<AdminUserStatisticsResponse>> adminUserStatistics(
            @RequestParam(required = false) LocalDate baseDate
    ) {

        return Response.success(adminDashboardQueryService.adminUserStatistics(baseDate), DashboardSuccessCode.DASHBOARD_ADMIN_USER_STATISTICS_OK);
    }

    @GetMapping("/clothes/statistics")
    public ResponseEntity<Response<AdminClothesStatisticsResponse>> adminClothesStatistics() {

        return Response.success(adminDashboardQueryService.adminClothesStatistics(), DashboardSuccessCode.DASHBOARD_ADMIN_CLOTHES_STATISTICS_OK);
    }

    @GetMapping("/sale-posts/statistics")
    public ResponseEntity<Response<AdminSalePostStatisticsResponse>> adminSalePostStatistics(
            @RequestParam(required = false) LocalDate baseDate
    ) {

        return Response.success(adminDashboardQueryService.adminSalePostStatistics(baseDate), DashboardSuccessCode.DASHBOARD_ADMIN_SALE_POST_STATISTICS_OK);
    }

    @GetMapping("/popular")
    public ResponseEntity<Response<AdminTopCategoryStatisticsResponse>> adminTopCategoryStatistics() {

        return Response.success(adminDashboardQueryService.adminTopCategoryStatistics(), DashboardSuccessCode.DASHBOARD_ADMIN_TOP10_CATEGORY_STATISTICS_OK);
    }
}
