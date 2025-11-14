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
import org.example.ootoutfitoftoday.common.util.DefaultLocationConstants;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.*;
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

    @Override
    public Slice<NotAuthSalePostListResponse> getNotAuthSalePostList(
            Long categoryId,
            SaleStatus status,
            String keyword,
            Pageable pageable
    ) {
        // 1. DTO 프로젝션을 위한 SQL 정의 (N+1 방지)
        String baseSql = """
                SELECT
                    s.id,
                    s.title,
                    s.price,
                    s.status,
                    s.trade_address,
                    ST_AsText(s.trade_location) AS trade_location,
                    (SELECT spi.image_url FROM sale_post_images spi WHERE spi.sale_post_id = s.id ORDER BY spi.display_order ASC LIMIT 1) AS thumbnail_url,
                    u.nickname AS seller_nickname,
                    c.name AS category_name,
                    s.created_at
                FROM sale_posts s
                JOIN users u ON s.user_id = u.id
                JOIN categories c ON s.category_id = c.id
                WHERE s.is_deleted = FALSE
                AND ST_Distance_Sphere(
                                  s.trade_location,
                                  ST_GeomFromText(:defaultPoint, 4326)
                              ) <= (:km * 1000)
                AND (:categoryId IS NULL OR s.category_id = :categoryId)
                AND (:status IS NULL OR s.status = :status)
                AND (:keyword IS NULL OR s.title LIKE :keyword OR s.content LIKE :keyword)
                """;

        // 2. ORDER BY 절 추가
        String finalSql = NativeQuerySortUtil.buildOrderClause(baseSql, pageable);

        // 3. Native Query 객체 생성 (엔티티 매핑 없이)
        Query query = entityManager.createNativeQuery(finalSql);

        query.setParameter("defaultPoint", DefaultLocationConstants.DEFAULT_TRADE_LOCATION);
        query.setParameter("km", DefaultLocationConstants.KM);
        query.setParameter("categoryId", categoryId);
        query.setParameter("status", status != null ? status.name() : null);

        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = "%" + keyword.trim() + "%";
        }
        query.setParameter("keyword", keyword);

        // 4. Slice 구현을 위한 LIMIT/OFFSET 설정
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize() + 1;

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        // 5. 쿼리 실행 및 결과 목록 획득 (Object[] 배열로 반환됨)
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // 6. Slice 객체 생성 로직 (hasNext 판단)
        boolean hasNext = results.size() > pageable.getPageSize();
        List<Object[]> content = hasNext ?
                results.subList(0, pageable.getPageSize()) :
                results;

        // 7. Object[] → DTO 변환
        List<NotAuthSalePostListResponse> responseContent = content.stream()
                .map(this::mapToNotAuthSalePostListResponse)
                .toList();

        return new SliceImpl<>(responseContent, pageable, hasNext);
    }

    // Object[] → NotAuthSalePostListResponse 변환 헬퍼 메서드
    private NotAuthSalePostListResponse mapToNotAuthSalePostListResponse(Object[] row) {
        // tradeLocation 파싱 (POINT(경도 위도) 형식)
        String tradeLocationStr = (String) row[5];
        org.example.ootoutfitoftoday.common.util.Location location =
                org.example.ootoutfitoftoday.common.util.PointFormatAndParse.parse(tradeLocationStr);

        return NotAuthSalePostListResponse.builder()
                .salePostId(((Number) row[0]).longValue())
                .title((String) row[1])
                .price((java.math.BigDecimal) row[2])
                .status(SaleStatus.valueOf((String) row[3]))
                .tradeAddress((String) row[4])
                .tradeLatitude(location.latitude())
                .tradeLongitude(location.longitude())
                .thumbnailUrl((String) row[6])
                .sellerNickname((String) row[7])
                .categoryName((String) row[8])
                .createdAt(((java.sql.Timestamp) row[9]).toLocalDateTime())
                .build();
    }
}
