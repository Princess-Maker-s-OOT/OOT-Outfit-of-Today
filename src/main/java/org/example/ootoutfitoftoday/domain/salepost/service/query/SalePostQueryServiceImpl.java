package org.example.ootoutfitoftoday.domain.salepost.service.query;

import com.ootcommon.salepost.enums.SaleStatus;
import com.ootcommon.salepost.response.SaleStatusCount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.CachedSliceResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostListResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostSummaryResponse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.salepost.service.cache.SalePostCacheService;
import org.example.ootoutfitoftoday.domain.salepost.util.NativeQuerySortUtil;
import org.example.ootoutfitoftoday.domain.salepost.util.SliceContent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalePostQueryServiceImpl implements SalePostQueryService {

    private final SalePostRepository salePostRepository;
    private final SalePostCacheService salePostCacheService;
    private final EntityManager entityManager;

    // 코드 중복 방지를 위한 헬퍼 메서드
    private static SliceContent sliceAndQueryResult(Query query, Pageable pageable) {
        // Slice 구현을 위한 LIMIT/OFFSET 설정
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize() + 1;

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        // 쿼리 실행 및 결과 목록 획득
        @SuppressWarnings("unchecked")
        List<SalePost> results = query.getResultList();

        // Slice 객체 생성 로직 (hasNext 판단)
        boolean hasNext = results.size() > pageable.getPageSize();
        List<SalePost> content = hasNext ?
                results.subList(0, pageable.getPageSize()) :
                results;

        return SliceContent.from(content, hasNext);
    }

    @Override
    public SalePost findSalePostById(Long salePostId) {

        return salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));
    }

    @Override
    public SalePostDetailResponse getSalePostDetail(Long salePostId) {

        SalePost salePost = findSalePostById(salePostId);

        return SalePostDetailResponse.from(salePost);
    }

    /**
     * 판매글 리스트 조회 (Cache-Aside 패턴)
     * - 캐시에 있으면 캐시에서 반환
     * - 캐시에 없으면 DB 조회 후 캐시에 저장
     */
    @Override
    public Slice<SalePostListResponse> getSalePostList(
            Long userId,
            Long categoryId,
            SaleStatus status,
            String keyword,
            Pageable pageable
    ) {
        // SalePostCacheService를 통해 캐시 적용
        CachedSliceResponse<SalePostListResponse> cached = salePostCacheService.getCachedSalePostList(
                userId, categoryId, status, keyword, pageable
        );
        return cached.toSlice();
    }

    @Override
    public long countByIsDeletedFalse() {

        return salePostRepository.countByIsDeletedFalse();
    }

    @Override
    public List<SaleStatusCount> saleStatusCounts() {

        return salePostRepository.saleStatusCounts();
    }

    @Override
    public int countSalePostsRegisteredSince(LocalDateTime start, LocalDateTime end) {

        return salePostRepository.countSalePostsRegisteredSince(start, end);
    }

    @Override
    public Slice<SalePostSummaryResponse> findMySalePosts(
            Long userId,
            SaleStatus status,
            Pageable pageable
    ) {
        // 1. ORDER BY 절이 없는 기본 SQL 정의
        String baseSql = """
                SELECT
                    s.id,
                    s.title,
                    s.content,
                    s.price,
                    s.status,
                    s.trade_address,
                    ST_AsText(s.trade_location) AS trade_location,
                    s.user_id,
                    s.category_id,
                    s.recommendation_id,
                    s.created_at,
                    s.updated_at,
                    s.is_deleted,
                    s.deleted_at
                FROM sale_posts s
                WHERE s.is_deleted = FALSE
                AND s.user_id = :userId
                AND (:status IS NULL OR s.status = :status)
                """;

        // 2. 유틸리티를 사용하여 ORDER BY 절이 동적으로 추가된 최종 SQL 문자열 획득
        // (이 로직 내에서 SQL 인젝션 검증 및 기본 정렬 설정이 완료됨)
        String finalSql = NativeQuerySortUtil.buildOrderClause(baseSql, pageable);

        // 3. Native Query 객체 생성 및 페이징 설정
        Query query = entityManager.createNativeQuery(finalSql, SalePost.class);

        query.setParameter("userId", userId);
        query.setParameter("status", status != null ? status.name() : null);

        SliceContent sliceContent = sliceAndQueryResult(query, pageable);

        List<SalePostSummaryResponse> responseContent = sliceContent.content().stream().map(SalePostSummaryResponse::from).toList();

        // 7. SliceImpl 반환
        return new SliceImpl<>(responseContent, pageable, sliceContent.hasNext());
    }

    // 추천 ID로 판매글 조회 (중복 방지용)
    @Override
    public Optional<SalePost> findByRecommendationId(Long recommendationId) {

        return salePostRepository.findByRecommendationIdAndIsDeletedFalse(recommendationId);
    }
}
