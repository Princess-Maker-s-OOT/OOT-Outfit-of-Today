package org.example.ootoutfitoftoday.domain.category.service.query;

import org.example.ootoutfitoftoday.domain.category.entity.Category;

import java.util.Optional;

public interface CategoryQueryService {

    Optional<Category> findById(long id);
}
