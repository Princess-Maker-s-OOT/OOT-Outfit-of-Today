package org.example.ootoutfitoftoday.domain.dashboard.service.query.admin;

import com.ootcommon.dashboard.constant.DashboardCacheNames;
import com.ootcommon.dashboard.response.AdminClothesStatisticsResponse;
import com.ootcommon.dashboard.response.AdminSalePostStatisticsResponse;
import com.ootcommon.dashboard.response.AdminTopCategoryStatisticsResponse;
import com.ootcommon.dashboard.response.AdminUserStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardAdminQueryServiceImpl implements DashboardAdminQueryService {

    private final CacheManager cacheManager;

    // Todo: 캐시매니저를 사용한 이유를 정리하기
    @Override
    public AdminUserStatisticsResponse adminUserStatistics(LocalDate baseDate) {

        LocalDate date = baseDate != null ? baseDate : LocalDate.now();

        String cacheKey = date.toString();

        var cache = cacheManager.getCache(DashboardCacheNames.USER);
        if (cache == null) {
            throw new IllegalStateException("해당 캐시 이름이 등록되어 있지 않습니다: " + DashboardCacheNames.USER);
        }

        AdminUserStatisticsResponse cached = cache.get(cacheKey, AdminUserStatisticsResponse.class);

        if (cached == null) {

            throw new IllegalArgumentException("캐싱 데이터가 존재하지 않아요.");
        }

        return cached;
    }

    // Todo: 수정 해야함.
    @Override
    public AdminClothesStatisticsResponse adminClothesStatistics() {

        var cache = cacheManager.getCache(DashboardCacheNames.CLOTHES);

        if (cache == null) {
            throw new IllegalStateException("의류 캐시가 등록되어 있지 않습니다: " + DashboardCacheNames.CLOTHES);
        }

        AdminClothesStatisticsResponse cached = cache.get("default", AdminClothesStatisticsResponse.class);

        if (cached == null) {

            throw new IllegalArgumentException("캐싱 데이터가 존재하지 않아요.");
        }

        return cached;
    }

    @Override
    public AdminSalePostStatisticsResponse adminSalePostStatistics(LocalDate baseDate) {

        LocalDate date = baseDate != null ? baseDate : LocalDate.now();

        String cacheKey = date.toString();

        var cache = cacheManager.getCache(DashboardCacheNames.SALE_POST);

        if (cache == null) {
            throw new IllegalStateException("해당 캐시 이름이 등록되어 있지 않습니다: " + DashboardCacheNames.SALE_POST);
        }

        AdminSalePostStatisticsResponse cached = cache.get(cacheKey, AdminSalePostStatisticsResponse.class);


        if (cached == null) {

            throw new IllegalArgumentException("캐싱 데이터가 존재하지 않아요.");
        }

        return cached;
    }

    // Todo: 수정 해야함.
    @Override
    public AdminTopCategoryStatisticsResponse adminTopCategoryStatistics() {

        var cache = cacheManager.getCache(DashboardCacheNames.CATEGORY);

        if (cache == null) {
            throw new IllegalStateException("카테고리 캐시가 등록되어 있지 않습니다: " + DashboardCacheNames.CATEGORY);
        }

        AdminTopCategoryStatisticsResponse cached = cache.get("default", AdminTopCategoryStatisticsResponse.class);

        if (cached == null) {

            throw new IllegalArgumentException("캐싱 데이터가 존재하지 않아요.");
        }

        return cached;
    }
}
