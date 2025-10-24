package org.example.ootoutfitoftoday.domain.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "카테고리 관리", description = "카테고리 관련 API")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    @Operation(
            summary = "카테고리 생성",
            description = "새로운 카테고리를 등록합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            }
    )
    @PostMapping("/admin/v1/categories")
    public ResponseEntity<Response<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {

        CategoryResponse response = categoryCommandService.createCategory(categoryRequest);

        return Response.success(response, CategorySuccessCode.CATEGORY_CREATED);
    }

    @Operation(
            summary = "카테고리 조회",
            description = "등록된 카테고리를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
            }
    )
    @GetMapping("/v1/categories")
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "한 페이지에 보여질 개수") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 컬럼") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") String direction
    ) {

        Page<CategoryResponse> categories = categoryQueryService.getCategories(
                page,
                size,
                sort,
                direction
        );

        return PageResponse.success(categories, CategorySuccessCode.CATEGORY_OK);
    }

    @Operation(
            summary = "카테고리 수정",
            description = "등록된 카테고리를 수정합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            }
    )
    @PutMapping("/admin/v1/categories/{categoryId}")
    public ResponseEntity<Response<CategoryResponse>> updateCategory(
            @Parameter(description = "수정할 카테고리 ID") @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {

        CategoryResponse categoryResponse = categoryCommandService.updateCategory(categoryId, categoryRequest);

        return Response.success(categoryResponse, CategorySuccessCode.CATEGORY_UPDATE);
    }

    @Operation(
            summary = "카테고리 삭제",
            description = "등록된 카테고리를 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            }
    )
    @DeleteMapping("/admin/v1/categories/{categoryId}")
    public ResponseEntity<Response<Void>> deleteCategory(
            @Parameter(description = "삭제할 카테고리 ID") @PathVariable Long categoryId
    ) {

        categoryCommandService.deleteCategory(categoryId);

        return Response.success(null, CategorySuccessCode.CATEGORY_DELETE);
    }
}