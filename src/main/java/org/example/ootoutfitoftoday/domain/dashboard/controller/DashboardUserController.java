package org.example.ootoutfitoftoday.domain.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/v1/dashboards/users")
public class DashboardUserController {

    private final DashboardUserQueryService dashboardUserQueryService;

    @Operation(
            summary = "대시보드 옷 분포 현황 조회",
            description = "사용자는 등록한 옷의 분포 현황을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/overview")
    public ResponseEntity<Response<DashboardUserSummaryResponse>> getUserDashboardSummary(
            @AuthenticationPrincipal AuthUser authUser
    ) {

        return Response.success(dashboardUserQueryService.getUserDashboardSummary(authUser.getUserId()), DashboardSuccessCode.DASHBOARD_USER_SUMMARY_OK);
    }

    @Operation(
            summary = "대시보드 옷의 착용 횟수 및 기간 통계 정보 조회",
            description = "사용자는 등록한 옷의 착용 횟수 및 기간 통계를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/statistics")
    public ResponseEntity<Response<DashboardUserWearStatisticsResponse>> getUserWearStatistics(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) LocalDate baseDate
    ) {

        return Response.success(dashboardUserQueryService.getUserWearStatistics(authUser.getUserId(), baseDate), DashboardSuccessCode.DASHBOARD_USER_STATISTICS_OK);
    }
}
