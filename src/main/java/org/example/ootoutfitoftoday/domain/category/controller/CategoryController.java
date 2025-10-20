package org.example.ootoutfitoftoday.domain.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiPageResponse;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.category.dto.request.CategoryRequest;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryResponse;
import org.example.ootoutfitoftoday.domain.category.exception.CategorySuccessCode;
import org.example.ootoutfitoftoday.domain.category.service.command.CategoryCommandService;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    @PostMapping("/admin/v1/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {

        CategoryResponse response = categoryCommandService.createCategory(categoryRequest);

        return ApiResponse.success(response, CategorySuccessCode.CATEGORY_CREATED);
    }

    @GetMapping("/v1/categories")
    public ResponseEntity<ApiPageResponse<CategoryResponse>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {

        Page<CategoryResponse> categories = categoryQueryService.getCategories(
                page,
                size,
                sort,
                direction
        );

        return ApiPageResponse.success(categories, CategorySuccessCode.CATEGORY_OK);
    }
}
