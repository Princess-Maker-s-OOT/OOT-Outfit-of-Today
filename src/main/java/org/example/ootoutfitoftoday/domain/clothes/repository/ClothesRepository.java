package org.example.ootoutfitoftoday.domain.clothes.repository;

import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClothesRepository extends JpaRepository<Clothes, Long>, CustomClothesRepository {

    Optional<Clothes> findByIdAndIsDeletedFalse(Long id);

    // 카테고리가 소프트 딜리트 처리될 때 연관된 옷의 카테고리 아이디 값을 null 처리
    @Modifying
    @Query("""
            UPDATE Clothes c
            SET c.category = NULL
            WHERE c.category.id IN :categoryIds
            """)
    void clearCategoryFromClothes(@Param("categoryIds") List<Long> categoryIds);

    // 삭제되지 않은 옷 카운터
    int countAllClothesByIsDeletedFalse();
}
