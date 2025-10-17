package org.example.ootoutfitoftoday.domain.category.service.command;

import org.example.ootoutfitoftoday.domain.category.dto.request.CategoryRequest;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryResponse;

public interface CategoryCommandService {

    void initializeCategories(); // 초기 데이터 삽입용

    CategoryResponse createCategory(CategoryRequest categoryRequest); // 카테고리 등록 api
}
