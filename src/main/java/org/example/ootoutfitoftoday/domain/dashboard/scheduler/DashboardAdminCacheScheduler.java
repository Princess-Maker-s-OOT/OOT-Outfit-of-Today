package org.example.ootoutfitoftoday.domain.dashboard.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.dashboard.service.query.admin.DashboardAdminQueryService;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DashboardAdminCacheScheduler {

    private final DashboardAdminQueryService dashboardAdminQueryService;
    private final CacheManager cacheManager; // CacheManager 주입

    // 1분마다 캐시 무효화 및 갱신 - 추후에 시간 조절 리팩토링할 예정
    @Scheduled(cron = "0 * * * * *")
    public void refreshDashboardCaches() {
        log.info("관리자 대시보드 캐시 무효화 시작");

        // 수동으로 캐시 무효화
        cacheManager.getCache("dashboard:admin:user").clear();
        cacheManager.getCache("dashboard:admin:clothes").clear();
        cacheManager.getCache("dashboard:admin:salePost").clear();
        cacheManager.getCache("dashboard:admin:category").clear();

        log.info("캐시 무효화 완료, 재생성 시작");

        // 캐시 재생성 (이 호출은 프록시를 통해 이루어짐)
        dashboardAdminQueryService.adminUserStatistics(LocalDate.now());
        dashboardAdminQueryService.adminClothesStatistics();
        dashboardAdminQueryService.adminSalePostStatistics(LocalDate.now());
        dashboardAdminQueryService.adminTopCategoryStatistics();

        log.info("관리자 대시보드 캐시 재생성 완료");
    }
}