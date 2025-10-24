package org.example.ootoutfitoftoday.domain.salepost.repository;

import org.example.ootoutfitoftoday.domain.salepost.dto.response.SaleStatusCount;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalePostRepository extends JpaRepository<SalePost, Long> {

    Optional<SalePost> findByIdAndIsDeletedFalse(Long salePostId);

    @Query("""
            SELECT sp FROM SalePost sp
            JOIN FETCH sp.user u
            JOIN FETCH sp.category c
            LEFT JOIN FETCH sp.images i
            WHERE sp.id = :salePostId
            AND sp.isDeleted = false
            """)
    Optional<SalePost> findByIdWithDetailsAndNotDeleted(@Param("salePostId") Long salePostId);

    @Query("""
            SELECT sp FROM SalePost sp
            JOIN FETCH sp.user u
            JOIN FETCH sp.category c
            WHERE sp.isDeleted = false
            AND (:categoryId IS NULL OR c.id = :categoryId)
            AND (:status IS NULL OR sp.status = :status)
            AND (:keyword IS NULL OR 
                LOWER(sp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
                LOWER(sp.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Slice<SalePost> findAllWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("status") SaleStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 삭제되지 않은 총 판매글 조회
    long countByIsDeletedFalse();

    // 상태별 판매글 조회
    @Query("""
            SELECT sp.status, count(sp)
            FROM SalePost sp
            WHERE sp.isDeleted = false
            GROUP BY sp.status
            """)
    List<SaleStatusCount> saleStatusCounts();

    // 신규 판매글 (기간별 집계)
    @Query("""
            SELECT count(sp)
            FROM SalePost sp
            WHERE sp.isDeleted = false
              AND sp.createdAt >= :start
              AND sp.createdAt < :end
            """)
    int countSalePostsRegisteredSince(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT sp FROM SalePost sp
            JOIN FETCH sp.user u
            JOIN FETCH sp.category c
            WHERE sp.user.id = :userId
            AND sp.isDeleted = false
            AND (:status IS NULL OR sp.status = :status)
            ORDER BY sp.createdAt DESC
            """)
    Slice<SalePost> findMyPosts(
            @Param("userId") Long userId,
            @Param("status") SaleStatus status,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO sale_posts 
            (title, content, price, status, 
            trade_address, trade_location, user_id, 
            category_id, is_deleted, created_at, updated_at)
            VALUES (?1, ?2, ?3, ?4, ?5, ST_GeomFromText(?6, 4326), ?7, ?8, ?9, NOW(), NOW())
            """, nativeQuery = true)
    void saveAsNativeQuery(
            String title,
            String content,
            BigDecimal price,
            String status,
            String tradeAddress,
            String tradeLocation,
            Long userId,
            Long categoryId,
            boolean isDeleted
    );

    @Query(value = """
            SELECT LAST_INSERT_ID()
            """, nativeQuery = true)
    Long findLastInsertId();
}
