package org.example.ootoutfitoftoday.domain.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.DashboardUserSummaryResponse;
import org.example.ootoutfitoftoday.domain.dashboard.exception.DashboardSuccessCode;
import org.example.ootoutfitoftoday.domain.dashboard.service.query.user.DashboardUserQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
