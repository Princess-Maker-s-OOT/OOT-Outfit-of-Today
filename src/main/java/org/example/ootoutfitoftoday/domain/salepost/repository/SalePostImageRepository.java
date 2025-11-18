package org.example.ootoutfitoftoday.domain.salepost.repository;

import org.example.ootoutfitoftoday.domain.salepost.entity.SalePostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalePostImageRepository extends JpaRepository<SalePostImage, Long> {

    @Query("""
            SELECT spi
            FROM SalePostImage spi
            WHERE spi.salePost.id = :salePostId
              AND spi.isDeleted = false
            """)
    List<SalePostImage> findBySalePostId(@Param("salePostId") Long salePostId);
}
