package org.example.ootoutfitoftoday.domain.salepost.service.query;

import org.example.ootoutfitoftoday.domain.salepost.dto.response.*;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalePostQueryService {

    SalePost findSalePostById(Long salePostId);

    SalePostDetailResponse getSalePostDetail(Long salePostId);

    Slice<SalePostListResponse> getSalePostList(
            Long userId,
            Long categoryId,
            SaleStatus status,
            String keyword,
            Pageable pageable
    );

    Slice<NotAuthSalePostListResponse> getNotAuthSalePostList(
            Long categoryId,
            SaleStatus status,
            String keyword,
            Pageable pageable
    );

    long countByIsDeletedFalse();

    List<SaleStatusCount> saleStatusCounts();

    int countSalePostsRegisteredSince(LocalDateTime start, LocalDateTime end);

    Slice<SalePostSummaryResponse> findMySalePosts(
            Long userId,
            SaleStatus status,
            Pageable pageable
    );

    // 추천 ID로 판매글 조회 (중복 방지용)
    Optional<SalePost> findByRecommendationId(Long recommendationId);
}
