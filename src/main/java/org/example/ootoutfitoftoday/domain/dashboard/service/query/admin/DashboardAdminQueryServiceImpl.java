package org.example.ootoutfitoftoday.domain.dashboard.service.query.admin;

import com.ootcommon.category.response.CategoryStat;
import com.ootcommon.clothes.response.ClothesColorCount;
import com.ootcommon.clothes.response.ClothesSizeCount;
import com.ootcommon.dashboard.constant.DashboardAdminCacheNames;
import com.ootcommon.dashboard.response.AdminClothesStatisticsResponse;
import com.ootcommon.dashboard.response.AdminSalePostStatisticsResponse;
import com.ootcommon.dashboard.response.AdminTopCategoryStatisticsResponse;
import com.ootcommon.dashboard.response.AdminUserStatisticsResponse;
import com.ootcommon.salepost.enums.SaleStatus;
import com.ootcommon.salepost.response.NewSalePost;
import com.ootcommon.salepost.response.SaleStatusCount;
import com.ootcommon.user.response.NewUsers;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.example.ootoutfitoftoday.domain.salepost.service.query.SalePostQueryService;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardAdminQueryServiceImpl implements DashboardAdminQueryService {

    private final UserQueryService userQueryService;
    private final ClothesQueryService clothesQueryService;
    private final SalePostQueryService salePostQueryService;

    @Override
    @Cacheable(
            value = DashboardAdminCacheNames.USER,
            key = "#baseDate != null ? #baseDate.toString() : T(java.time.LocalDate).now().toString()",
            unless = "#result == null"
    )
    public AdminUserStatisticsResponse adminUserStatistics(LocalDate baseDate) {

        if (baseDate == null) {
            baseDate = LocalDate.now();
        }

        int totalUsers = userQueryService.countAllUsers(); // 전체 유저 수
        int activeUsers = userQueryService.countByIsDeleted(false); // 활성 유저 수
        int deletedUsers = userQueryService.countByIsDeleted(true); // 비활성 유저 수

        LocalDateTime startOfDay = baseDate.atStartOfDay(); // 기준이 되는 날 00시 00분 00초
        LocalDateTime endOfDay = baseDate.plusDays(1).atStartOfDay(); // 기준일 기준 다음 날 00시 00분 00초
        LocalDateTime startOfWeek = baseDate.with(DayOfWeek.MONDAY).atStartOfDay(); // 기준일 기준 월요일 00시 00분 00초
        LocalDateTime startOfMonth = baseDate.withDayOfMonth(1).atStartOfDay(); // 기준일 해당 월 1일 00시 00분 00초

        /**
         * 오늘 00:00 ~ 내일 00:00 미만
         * 이번 주 월요일 00:00 ~ 내일 00:00 미만
         * 이번 달 1일 00:00 ~ 내일 00:00 미만
         */
        int daily = userQueryService.countUsersRegisteredSince(startOfDay, endOfDay);
        int weekly = userQueryService.countUsersRegisteredSince(startOfWeek, endOfDay);
        int monthly = userQueryService.countUsersRegisteredSince(startOfMonth, endOfDay);

        NewUsers newUsers = new NewUsers(daily, weekly, monthly);

        return new AdminUserStatisticsResponse(totalUsers, activeUsers, deletedUsers, newUsers);
    }

    @Override
    @Cacheable(value = DashboardAdminCacheNames.CLOTHES, key = "'default'", unless = "#result == null")
    public AdminClothesStatisticsResponse adminClothesStatistics() {

        long totalClothes = clothesQueryService.countClothesByIsDeletedFalse(); // 전체 옷 수량

        List<CategoryStat> categoryStats = clothesQueryService.countTopCategoryStats(); // 카테고리별 옷 수량

        List<ClothesColorCount> clothesColors = clothesQueryService.clothesColorsCount(); // 색상별 옷 수량

        List<ClothesSizeCount> clothesSizes = clothesQueryService.clothesSizesCount(); // 사이즈별 옷 수량

        return new AdminClothesStatisticsResponse(
                totalClothes,
                categoryStats,
                clothesColors,
                clothesSizes
        );
    }

    @Override
    @Cacheable(
            value = DashboardAdminCacheNames.SALE_POST,
            key = "#baseDate != null ? #baseDate.toString() : T(java.time.LocalDate).now().toString()",
            unless = "#result == null"
    )
    public AdminSalePostStatisticsResponse adminSalePostStatistics(LocalDate baseDate) {

        if (baseDate == null) {
            baseDate = LocalDate.now();
        }

        // 판매글 총 수량
        long totalSales = salePostQueryService.countByIsDeletedFalse();

        // 상태별 판매글 수량
        List<SaleStatusCount> saleStatusCounts = salePostQueryService.saleStatusCounts();

        EnumMap<SaleStatus, Long> countMap = new EnumMap<>(SaleStatus.class);
        for (SaleStatusCount saleStatus : saleStatusCounts) {
            countMap.put(saleStatus.getSaleStatus(), saleStatus.getCount());
        }

        List<SaleStatusCount> orderBySaleStatus = Arrays.stream(SaleStatus.values())
                .map(
                        saleStatus -> new SaleStatusCount(saleStatus, countMap.getOrDefault(saleStatus, 0L))
                )
                .toList();

        // 일, 주, 월별 통계 수량
        LocalDateTime startOfDay = baseDate.atStartOfDay(); // 기준이 되는 날 00시 00분 00초
        LocalDateTime endOfDay = baseDate.plusDays(1).atStartOfDay(); // 기준일 기준 다음 날 00시 00분 00초
        LocalDateTime startOfWeek = baseDate.with(DayOfWeek.MONDAY).atStartOfDay(); // 기준일 기준 월요일 00시 00분 00초
        LocalDateTime startOfMonth = baseDate.withDayOfMonth(1).atStartOfDay(); // 기준일 해당 월 1일 00시 00분 00초

        int daily = salePostQueryService.countSalePostsRegisteredSince(startOfDay, endOfDay);
        int weekly = salePostQueryService.countSalePostsRegisteredSince(startOfWeek, endOfDay);
        int monthly = salePostQueryService.countSalePostsRegisteredSince(startOfMonth, endOfDay);

        NewSalePost newSalePost = new NewSalePost(
                daily,
                weekly,
                monthly
        );

        return new AdminSalePostStatisticsResponse(
                totalSales,
                orderBySaleStatus,
                newSalePost
        );
    }

    @Override
    @Cacheable(value = DashboardAdminCacheNames.CATEGORY, key = "'default'", unless = "#result == null")
    public AdminTopCategoryStatisticsResponse adminTopCategoryStatistics() {

        return new AdminTopCategoryStatisticsResponse(clothesQueryService.findTopCategoryStats());
    }
}