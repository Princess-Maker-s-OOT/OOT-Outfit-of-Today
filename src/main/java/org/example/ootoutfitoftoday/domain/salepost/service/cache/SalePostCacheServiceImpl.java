package org.example.ootoutfitoftoday.domain.salepost.service.cache;

import com.ootcommon.salepost.enums.SaleStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.util.DefaultLocationConstants;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.CachedSliceResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostListResponse;
import org.example.ootoutfitoftoday.domain.salepost.util.NativeQuerySortUtil;
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
public class SalePostCacheServiceImpl implements SalePostCacheService {

    private final UserQueryService userQueryService;
    private final EntityManager entityManager;

    /**
     * 판매글 리스트 조회 (캐시 적용)
     * - Cache-Aside 패턴
     * - Redis에 캐시 저장/조회
     */
    @Override
    @Cacheable(
            value = "salePostListCache",
            key = "{#userId, #categoryId, #status, #keyword, #pageable}",
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
                                  ST_GeomFromText(:userPoint, 4326)
                              ) <= (:km * 1000)
                AND (:categoryId IS NULL OR s.category_id = :categoryId)
                AND (:status IS NULL OR s.status = :status)
                AND (:keyword IS NULL OR s.title LIKE :keyword OR s.content LIKE :keyword)
                """;

        // 2. ORDER BY 절 추가
        String finalSql = NativeQuerySortUtil.buildOrderClause(baseSql, pageable);

        // 3. Native Query 객체 생성 (엔티티 매핑 없이)
        Query query = entityManager.createNativeQuery(finalSql);

        query.setParameter("userPoint", user.getTradeLocation());
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

        // 5. 쿼리 실행 및 결과 목록 획득 (Object[] 리스트)
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // 6. Slice 객체 생성 로직 (hasNext 판단)
        boolean hasNext = results.size() > pageable.getPageSize();
        List<Object[]> content = hasNext ? results.subList(0, pageable.getPageSize()) : results;

        // 7. Object[]를 SalePostListResponse DTO로 직접 변환
        List<SalePostListResponse> responseContent = content.stream()
                .map(row -> {
                    org.example.ootoutfitoftoday.common.util.Location location = org.example.ootoutfitoftoday.common.util.PointFormatAndParse.parse((String) row[5]);
                    return new SalePostListResponse(
                            ((Number) row[0]).longValue(),
                            (String) row[1],
                            (java.math.BigDecimal) row[2],
                            SaleStatus.valueOf((String) row[3]),
                            (String) row[4],
                            location.latitude(),
                            location.longitude(),
                            (String) row[6],
                            (String) row[7],
                            (String) row[8],
                            ((java.sql.Timestamp) row[9]).toLocalDateTime()
                    );
                })
                .toList();

        return new CachedSliceResponse<>(
                responseContent,
                hasNext,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
    }
}
