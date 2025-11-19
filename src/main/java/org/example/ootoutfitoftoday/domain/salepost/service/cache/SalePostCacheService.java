package org.example.ootoutfitoftoday.domain.salepost.service.cache;

import com.ootcommon.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.CachedSliceResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostListResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.data.domain.Pageable;

public interface SalePostCacheService {

    CachedSliceResponse<SalePostListResponse> getCachedSalePostList(
            User user,
            Long latitude,
            Long longitude,
            Long categoryId,
            SaleStatus status,
            String keyword,
            Pageable pageable
    );
}
