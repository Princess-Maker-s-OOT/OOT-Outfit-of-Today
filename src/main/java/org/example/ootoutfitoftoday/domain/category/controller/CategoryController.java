package org.example.ootoutfitoftoday.domain.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
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
    public ResponseEntity<Response<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {

        CategoryResponse response = categoryCommandService.createCategory(categoryRequest);

        return Response.success(response, CategorySuccessCode.CATEGORY_CREATED);
    }

    @GetMapping("/v1/categories")
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(
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

        return PageResponse.success(categories, CategorySuccessCode.CATEGORY_OK);
    }

    @PutMapping("/admin/v1/categories/{categoryId}")
    public ResponseEntity<Response<CategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {

        CategoryResponse categoryResponse = categoryCommandService.updateCategory(categoryId, categoryRequest);

        return Response.success(categoryResponse, CategorySuccessCode.CATEGORY_UPDATE);
    }

    @DeleteMapping("/admin/v1/categories/{categoryId}")
    public ResponseEntity<Response<Void>> deleteCategory(
            @PathVariable Long categoryId
    ) {

        categoryCommandService.deleteCategory(categoryId);

        return Response.success(null, CategorySuccessCode.CATEGORY_DELETE);
    }
}
