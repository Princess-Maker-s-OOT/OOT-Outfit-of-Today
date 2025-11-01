package org.example.ootoutfitoftoday.domain.clothes.repository;

import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClothesRepository extends JpaRepository<Clothes, Long>, ClothesCustomRepository {

    @Query("""
            SELECT DISTINCT c
            FROM Clothes c
            LEFT JOIN FETCH c.images ci
            LEFT JOIN FETCH ci.image i
            WHERE c.id = :id
              AND c.isDeleted = false
              AND (ci.isDeleted = false OR ci.isDeleted IS NULL)
              AND (i.isDeleted = false OR i.isDeleted IS NULL)
            """)
    Optional<Clothes> findByIdAndIsDeletedFalse(@Param("id") Long id);

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

    int countAllClothesByUserIdAndIsDeletedFalse(Long userId);

    @Query("""
            SELECT c.category.name, count(c)
            FROM Clothes c
            where c.user.id = :userId
            group by c.category.id, c.category.name
            order by count(c) desc, c.category.id
            limit 10
            """)
    List<CategoryStat> countUserTopCategoryStats(@Param("userId") Long userId);

    // 사용자의 모든 옷 조회
    @Query("""
            SELECT c
            FROM Clothes c
            WHERE c.user.id = :userId
              AND c.isDeleted = false
            """)
    List<Clothes> findAllByUserIdAndIsDeletedFalse(@Param("userId") Long userId);
}