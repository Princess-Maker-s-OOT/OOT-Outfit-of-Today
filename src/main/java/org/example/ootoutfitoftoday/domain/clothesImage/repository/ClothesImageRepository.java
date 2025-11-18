package org.example.ootoutfitoftoday.domain.clothesImage.repository;

import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClothesImageRepository extends JpaRepository<ClothesImage, Long> {

    @Modifying(clearAutomatically = false, flushAutomatically = true)
    @Query("""
            UPDATE ClothesImage ci
            SET ci.isDeleted = true,
                ci.deletedAt = CURRENT_TIMESTAMP
            WHERE ci.clothes.id = :clothesId
              AND ci.isDeleted = false
            """)
    int softDeleteAllByClothesId(@Param("clothesId") Long clothesId);

    @Query("""
            SELECT ci
            FROM ClothesImage ci
            JOIN FETCH ci.image
            WHERE ci.clothes.id = :clothesId and ci.isDeleted = false
            ORDER BY ci.updatedAt desc, ci.clothes.id asc
            """)
    List<ClothesImage> findByClothesId(Long clothesId);

    @Query("""
            SELECT EXISTS (
                    SELECT ci.id
                    FROM ClothesImage ci
                    WHERE ci.image.id In :imageIds
                      AND ci.isDeleted = false
                      AND ci.clothes.id <> :clothesId
                  )
            """)
    boolean existsLinkedImages(@Param("clothesId") Long clothesId, @Param("imageIds") List<Long> imageIds);

    @Query("""
            SELECT ci
            FROM ClothesImage ci
            WHERE ci.clothes.id = :clothesId
              AND ci.image.id IN :imageIds
              AND ci.isDeleted = true
            """)
    List<ClothesImage> findDeletedByClothesIdAndImageIds(@Param("clothesId") Long clothesId, @Param("imageIds") List<Long> imageIds);

    @Query("""
            SELECT ci
            FROM ClothesImage ci
            WHERE ci.clothes.id = :clothesId
              AND ci.image.id IN :imageIds
              AND ci.isDeleted = false
            """)
    List<ClothesImage> findByClothesIdAndImageIdsAndIsDeletedFalse(@Param("clothesId") Long clothesId, @Param("imageIds") List<Long> imageIds);

    @Query("""
            SELECT ci
            FROM ClothesImage ci
            WHERE ci.clothes.id = :clothesId
              AND ci.isDeleted = false
            ORDER BY ci.createdAt asc , ci.clothes.id asc
            """)
    List<ClothesImage> findByClothesIdAndIsDeletedFalseOrderByCreatedAtAsc(@Param("clothesId") Long clothesId);
}