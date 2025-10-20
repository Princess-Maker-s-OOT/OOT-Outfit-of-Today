package org.example.ootoutfitoftoday.domain.salepost.repository;

import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
        SELECT DISTINCT sp FROM SalePost sp
        JOIN FETCH sp.user u
        JOIN FETCH sp.category c
        WHERE sp.isDeleted = false
        AND (:categoryId IS NULL OR c.id = :categoryId)
        AND (:status IS NULL OR sp.status = :status)
        AND (:keyword IS NULL OR 
            LOWER(sp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
            LOWER(sp.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY sp.createdAt DESC
        """)
    Slice<SalePost> findAllWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("status") SaleStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
