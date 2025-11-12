package org.example.ootoutfitoftoday.domain.category.service.command;

import org.example.ootoutfitoftoday.domain.category.dto.request.CategoryRequest;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryResponse;

public interface CategoryCommandService {

    CategoryResponse createCategory(CategoryRequest categoryRequest); // 카테고리 등록 api

    CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest);

    void deleteCategory(Long id);
}
