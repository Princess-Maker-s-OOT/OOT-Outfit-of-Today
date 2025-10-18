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
@RequestMapping("/v1/categories")
public class CategoryController {

    private final CategoryCommandService categoryCommandService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {
        CategoryResponse response = categoryCommandService.createCategory(categoryRequest);

        return ApiResponse.success(response,CategorySuccessCode.CATEGORY_CREATED);
    }
}
