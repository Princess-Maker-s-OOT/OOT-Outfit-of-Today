package org.example.ootoutfitoftoday.domain.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.category.dto.request.CategoryRequest;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryResponse;
import org.example.ootoutfitoftoday.domain.category.exception.CategorySuccessCode;
import org.example.ootoutfitoftoday.domain.category.service.command.CategoryCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryCommandService categoryCommandService;

    @PostMapping("/admin/v1/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {
        CategoryResponse response = categoryCommandService.createCategory(categoryRequest);

        return ApiResponse.success(response,CategorySuccessCode.CATEGORY_CREATED);
    }
}
