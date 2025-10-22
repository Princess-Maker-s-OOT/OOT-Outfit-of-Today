package org.example.ootoutfitoftoday.domain.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
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
@RequestMapping("/admin/v1/dashboards/users")
public class AdminDashboardController {

    private final AdminDashboardQueryService adminDashboardQueryService;

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AdminUserStatisticsResponse>> adminUserStatistics(
            @RequestParam(required = false) LocalDate baseDate
    ) {

        return ApiResponse.success(adminDashboardQueryService.adminUserStatistics(baseDate), DashboardSuccessCode.DASHBOARD_ADMIN_USER_STATISTICS_OK);
    }
}
