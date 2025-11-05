package org.example.ootoutfitoftoday.domain.dashboard.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.DashboardUserSummaryResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.DashboardUserWearStatisticsResponse;
import org.example.ootoutfitoftoday.domain.dashboard.exception.DashboardSuccessCode;
import org.example.ootoutfitoftoday.domain.dashboard.service.query.user.DashboardUserQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "사용자 대시보드", description = "사용자가 확인하는 통계 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/dashboards/users/overview")
public class DashboardUserController {

    private final DashboardUserQueryService dashboardUserQueryService;

    @GetMapping
    public ResponseEntity<Response<DashboardUserSummaryResponse>> getUserDashboardSummary(
            @AuthenticationPrincipal AuthUser authUser
    ) {

        return Response.success(dashboardUserQueryService.getUserDashboardSummary(authUser.getUserId()), DashboardSuccessCode.DASHBOARD_USER_SUMMARY_OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Response<DashboardUserWearStatisticsResponse>> getUserWearStatistics(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) LocalDate baseDate
    ) {

        return Response.success(dashboardUserQueryService.getUserWearStatistics(authUser.getUserId(), baseDate), DashboardSuccessCode.DASHBOARD_USER_STATISTICS_OK);
    }

}
