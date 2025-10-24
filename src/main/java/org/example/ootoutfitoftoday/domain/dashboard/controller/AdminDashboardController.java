package org.example.ootoutfitoftoday.domain.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "관리자 대시보드", description = "관리자가 확인하는 통계 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/v1/dashboards")
public class AdminDashboardController {

    private final AdminDashboardQueryService adminDashboardQueryService;

    @Operation(
            summary = "대시보드 유저 통계자료 조회",
            description = "관리자는 누적 가입자 수, 활성/비활성 사용자 수 및 일간/ 주간/ 월간 신규 가입자를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/users/statistics")
    public ResponseEntity<Response<AdminUserStatisticsResponse>> adminUserStatistics(
            @RequestParam(required = false) LocalDate baseDate
    ) {

        return Response.success(adminDashboardQueryService.adminUserStatistics(baseDate), DashboardSuccessCode.DASHBOARD_ADMIN_USER_STATISTICS_OK);
    }

    @Operation(
            summary = "대시보드 옷 통계자료 조회",
            description = "관리자는 유저가 등록한 옷의 통계자료를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/clothes/statistics")
    public ResponseEntity<Response<AdminClothesStatisticsResponse>> adminClothesStatistics() {

        return Response.success(adminDashboardQueryService.adminClothesStatistics(), DashboardSuccessCode.DASHBOARD_ADMIN_CLOTHES_STATISTICS_OK);
    }

    @Operation(
            summary = "대시보드 판매글 통계자료 조회",
            description = "관리자는 등록된 판매글 수 및 거래현황을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/sale-posts/statistics")
    public ResponseEntity<Response<AdminSalePostStatisticsResponse>> adminSalePostStatistics(
            @RequestParam(required = false) LocalDate baseDate
    ) {

        return Response.success(adminDashboardQueryService.adminSalePostStatistics(baseDate), DashboardSuccessCode.DASHBOARD_ADMIN_SALE_POST_STATISTICS_OK);
    }

    @Operation(
            summary = "대시보드 카테고리 통계자료 조회",
            description = "관리자는 인기 카테고리를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/popular")
    public ResponseEntity<Response<AdminTopCategoryStatisticsResponse>> adminTopCategoryStatistics() {

        return Response.success(adminDashboardQueryService.adminTopCategoryStatistics(), DashboardSuccessCode.DASHBOARD_ADMIN_TOP10_CATEGORY_STATISTICS_OK);
    }
}
