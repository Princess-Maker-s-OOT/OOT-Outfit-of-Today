package org.example.ootoutfitoftoday.domain.dashboard.service.query.admin;

import com.ootcommon.dashboard.constant.DashboardAdminCacheNames;
import com.ootcommon.dashboard.response.AdminClothesStatisticsResponse;
import com.ootcommon.dashboard.response.AdminSalePostStatisticsResponse;
import com.ootcommon.dashboard.response.AdminTopCategoryStatisticsResponse;
import com.ootcommon.dashboard.response.AdminUserStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException; // 키 값이 필수인데 없으면 던지는 예외를 처리하기 위해 사용

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardAdminQueryServiceImpl implements DashboardAdminQueryService {

    private final CacheManager cacheManager;

    @Override
    public AdminUserStatisticsResponse adminUserStatistics(LocalDate baseDate) {
        LocalDate date = baseDate != null ? baseDate : LocalDate.now();
        String cacheKey = date.toString();

        var cache = cacheManager.getCache(DashboardAdminCacheNames.USER);
        
        if (cache == null) {
            throw new IllegalStateException("해당 캐시 이름이 등록되어 있지 않습니다: " + DashboardAdminCacheNames.USER);
        }
        
        AdminUserStatisticsResponse cached = cache.get(cacheKey, AdminUserStatisticsResponse.class);
        
        if (cached == null) {
            throw new NoSuchElementException("캐시된 사용자 통계 데이터가 없습니다.");
        }
        
        return cached;
    }

    @Override
    public AdminClothesStatisticsResponse adminClothesStatistics() {
        var cache = cacheManager.getCache(DashboardAdminCacheNames.CLOTHES);
       
        if (cache == null) {
            throw new IllegalStateException("의류 캐시가 등록되어 있지 않습니다: " + DashboardAdminCacheNames.CLOTHES);
        }
        
        AdminClothesStatisticsResponse cached = cache.get("default", AdminClothesStatisticsResponse.class);
        if (cached == null) {
            throw new NoSuchElementException("캐시된 의류 통계 데이터가 없습니다.");
        }
        
        return cached;
    }

    @Override
    public AdminSalePostStatisticsResponse adminSalePostStatistics(LocalDate baseDate) {
        LocalDate date = baseDate != null ? baseDate : LocalDate.now();
        String cacheKey = date.toString();

        var cache = cacheManager.getCache(DashboardAdminCacheNames.SALE_POST);
        
        if (cache == null) {
            throw new IllegalStateException("해당 캐시 이름이 등록되어 있지 않습니다: " + DashboardAdminCacheNames.SALE_POST);
        }
        
        AdminSalePostStatisticsResponse cached = cache.get(cacheKey, AdminSalePostStatisticsResponse.class);
        
        if (cached == null) {
            throw new NoSuchElementException("캐시된 판매글 통계 데이터가 없습니다.");
        }
        
        return cached;
    }

    @Override
    public AdminTopCategoryStatisticsResponse adminTopCategoryStatistics() {
        var cache = cacheManager.getCache(DashboardAdminCacheNames.CATEGORY);
        
        if (cache == null) {
            throw new IllegalStateException("카테고리 캐시가 등록되어 있지 않습니다: " + DashboardAdminCacheNames.CATEGORY);
        }
        
        AdminTopCategoryStatisticsResponse cached = cache.get("default", AdminTopCategoryStatisticsResponse.class);
        
        if (cached == null) {
            throw new NoSuchElementException("캐시된 카테고리 통계 데이터가 없습니다.");
        }
        
        return cached;
    }
}
