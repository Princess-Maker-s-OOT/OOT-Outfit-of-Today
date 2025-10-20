package org.example.ootoutfitoftoday.domain.category.repository;

import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 상위 계층의 ID 값이 null인 값을 카운트
    long countByParentIsNull();

    Page<Category> findAllByIsDeletedFalse(Pageable pageable);
}
