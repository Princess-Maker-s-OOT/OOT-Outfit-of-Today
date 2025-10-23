package org.example.ootoutfitoftoday.domain.dashboard.service.query.admin;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesColorCount;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesSizeCount;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminClothesStatisticsResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminSalePostStatisticsResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminUserStatisticsResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.NewSalePost;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SaleStatusCount;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.salepost.service.query.SalePostQueryService;
import org.example.ootoutfitoftoday.domain.user.dto.response.NewUsers;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
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
public class AdminDashboardQueryServiceImpl implements AdminDashboardQueryService {

    private final UserQueryService userQueryService;
    private final ClothesQueryService clothesQueryService;
    private final SalePostQueryService salePostQueryService;

    @Override
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
}
