package org.example.ootoutfitoftoday.domain.salepost.repository;

import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
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
}
