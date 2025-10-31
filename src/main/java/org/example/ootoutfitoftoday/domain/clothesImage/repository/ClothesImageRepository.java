package org.example.ootoutfitoftoday.domain.clothesImage.repository;

import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClothesImageRepository extends JpaRepository<ClothesImage, Long> {

    // 추후에 로그도 찍을 것을 고려하여 반환 타입 int로 구현
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

    // 옷-이미지 연결되어 있지만 softDelete 처리된 데이터들
    @Query("""
            SELECT ci
            FROM ClothesImage ci
            WHERE ci.clothes.id = :clothesId
              AND ci.image.id IN :imageIds
              AND ci.isDeleted = true
            """)
    List<ClothesImage> findDeletedByClothesIdAndImageIds(@Param("clothesId") Long clothesId, @Param("imageIds") List<Long> imageIds);
}
