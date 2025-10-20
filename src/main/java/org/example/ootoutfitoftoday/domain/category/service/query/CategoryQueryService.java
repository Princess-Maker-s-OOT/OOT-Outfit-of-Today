package org.example.ootoutfitoftoday.domain.category.service.query;

import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryResponse;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface CategoryQueryService {

    Optional<Category> findById(long id);

    Page<CategoryResponse> getCategories(
            int page,
            int size,
            String sort,
            String direction
    );
}
