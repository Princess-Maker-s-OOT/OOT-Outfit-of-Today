package org.example.ootoutfitoftoday.domain.dashboard.service.query.admin;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.CountClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.CountClothesSize;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminClothesStatisticsResponse;
import org.example.ootoutfitoftoday.domain.dashboard.dto.response.AdminUserStatisticsResponse;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardQueryServiceImpl implements AdminDashboardQueryService {

    private final UserQueryService userQueryService;
    private final ClothesQueryService clothesQueryService;

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

        AdminUserStatisticsResponse.NewUsers newUsers = new AdminUserStatisticsResponse.NewUsers(daily, weekly, monthly);

        return new AdminUserStatisticsResponse(totalUsers, activeUsers, deletedUsers, newUsers);
    }

    @Override
    public AdminClothesStatisticsResponse adminClothesStatistics() {

        int totalClothes = clothesQueryService.countClothesByIsDeletedFalse(); // 전체 옷 수량

        List<CategoryStat> categoryStats = clothesQueryService.countTopCategoryStats(); // 카테고리별 옷 수량

        List<CountClothesColor> ClothesColors = clothesQueryService.countClothesColors(); // 색상별 옷 수량

        List<CountClothesSize> ClothesSizes = clothesQueryService.countClothesSizes(); // 사이즈별 옷 수량

        return new AdminClothesStatisticsResponse(
                totalClothes,
                categoryStats,
                ClothesColors,
                ClothesSizes
        );
    }
}
