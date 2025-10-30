package org.example.ootoutfitoftoday.domain.clothesImage.repository;

import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClothesImageRepository extends JpaRepository<ClothesImage, Long> {

    @Query("""
            SELECT ci
            FROM ClothesImage ci
            JOIN FETCH ci.image
            WHERE ci.clothes.id = :clothesId and ci.isDeleted = false
            """)
    List<ClothesImage> findByClothesId(Long clothesId);
}
