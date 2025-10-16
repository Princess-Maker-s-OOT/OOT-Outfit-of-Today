package org.example.ootoutfitoftoday.domain.category.repository;

import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
