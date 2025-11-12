package org.example.ootoutfitoftoday.domain.category.repository;

import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 상위 계층의 ID 값이 null인 값을 카운트
    long countByParentIsNull();

    Page<Category> findAllByIsDeletedFalse(Pageable pageable);

    Optional<Category> findByIdAndIsDeletedFalse(Long id);

    @Query("""
            SELECT c.id
            FROM Category c
            WHERE c.parent.id IN :parentIds
              AND c.isDeleted = false
            """)
    List<Long> findIdsByParentIds(@Param("parentIds") List<Long> parentIds);

    @Modifying
    @Query("""
            UPDATE Category c
            SET c.isDeleted = true,
                c.deletedAt = CURRENT_TIMESTAMP
            WHERE c.id IN :categoryIds
            """)
    void softDeleteCategories(@Param("categoryIds") List<Long> categoryIds);
}
