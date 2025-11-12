package org.example.ootoutfitoftoday.domain.salepost.service.cache;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.util.DefaultLocationConstants;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.CachedSliceResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostListResponse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.salepost.util.NativeQuerySortUtil;
import org.example.ootoutfitoftoday.domain.salepost.util.SliceContent;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 판매글 캐시 전용 서비스
 * - 캐시 로직과 비즈니스 로직 분리
 * - 순환 참조 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalePostCacheService {

    private final UserQueryService userQueryService;
    private final EntityManager entityManager;

    /**
     * 판매글 리스트 조회 (캐시 적용)
     * - Cache-Aside 패턴
     * - Redis에 캐시 저장/조회
     */
    @Cacheable(
            value = "salePostListCache",
            key = "#userId + ':' + #categoryId + ':' + " +
                  "(#status != null ? #status.name() : 'null') + ':' + " +
                  "(#keyword != null ? #keyword : 'null') + ':' + " +
                  "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + " +
                  "#pageable.sort.toString()",
            unless = "#result == null || #result.content.isEmpty()"
    )
    public CachedSliceResponse<SalePostListResponse> getCachedSalePostList(
            Long userId,
            Long categoryId,
            SaleStatus status,
            String keyword,
            Pageable pageable
    ) {
        User user = userQueryService.findByIdAsNativeQuery(userId);

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
                    s.deleted_at,
                    u.nickname AS seller_nickname,
                    c.name AS category_name
                FROM sale_posts s
                JOIN users u ON s.user_id = u.id
                JOIN categories c ON s.category_id = c.id
                WHERE s.is_deleted = FALSE
                AND ST_Distance_Sphere(
                                  s.trade_location,
                                  ST_GeomFromText(:userPoint, 4326)
                              ) <= (:km * 1000)
                AND (:categoryId IS NULL OR s.category_id = :categoryId)
                AND (:status IS NULL OR s.status = :status)
                AND (:keyword IS NULL OR s.title LIKE :keyword OR s.content LIKE :keyword)
                """;

        // 2. 유틸리티를 사용하여 ORDER BY 절이 동적으로 추가된 최종 SQL 문자열 획득
        String finalSql = NativeQuerySortUtil.buildOrderClause(baseSql, pageable);

        // 3. Native Query 객체 생성 및 페이징 설정
        Query query = entityManager.createNativeQuery(finalSql, SalePost.class);

        query.setParameter("userPoint", user.getTradeLocation());
        query.setParameter("km", DefaultLocationConstants.KM);
        query.setParameter("categoryId", categoryId);
        query.setParameter("status", status != null ? status.name() : null);

        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = "%" + keyword.trim() + "%";
        }
        query.setParameter("keyword", keyword);

        SliceContent sliceContent = sliceAndQueryResult(query, pageable);

        List<SalePostListResponse> responseContent = sliceContent.content().stream()
                .map(SalePostListResponse::from)
                .toList();

        return new CachedSliceResponse<>(
                responseContent,
                sliceContent.hasNext(),
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
    }

    // 코드 중복 방지를 위한 헬퍼 메서드
    private static SliceContent sliceAndQueryResult(Query query, Pageable pageable) {
        // 4. Slice 구현을 위한 LIMIT/OFFSET 설정
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize() + 1;

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        // 5. 쿼리 실행 및 결과 목록 획득
        @SuppressWarnings("unchecked")
        List<SalePost> results = query.getResultList();

        // 6. Slice 객체 생성 로직 (hasNext 판단)
        boolean hasNext = results.size() > pageable.getPageSize();
        List<SalePost> content = hasNext ?
                results.subList(0, pageable.getPageSize()) :
                results;

        return SliceContent.from(content, hasNext);
    }
}
