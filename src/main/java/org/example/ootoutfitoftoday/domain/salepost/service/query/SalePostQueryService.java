package org.example.ootoutfitoftoday.domain.salepost.service.query;

import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostListResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SaleStatusCount;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

public interface SalePostQueryService {

    SalePost findSalePostById(Long salePostId);

    SalePostDetailResponse getSalePostDetail(Long salePostId);

    Slice<SalePostListResponse> getSalePostList(
            Long categoryId,
            SaleStatus status,
            String keyword,
            Pageable pageable
    );

    long countByIsDeletedFalse();

    List<SaleStatusCount> saleStatusCounts();

    int countSalePostsRegisteredSince(LocalDateTime start, LocalDateTime end);
}
