package org.example.ootoutfitoftoday.domain.category.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.category.dto.request.CategoryRequest;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryResponse;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.exception.CategoryErrorCode;
import org.example.ootoutfitoftoday.domain.category.exception.CategoryException;
import org.example.ootoutfitoftoday.domain.category.repository.CategoryRepository;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandServiceImpl implements CategoryCommandService {

    private final CategoryRepository categoryRepository;
    private final ClothesCommandService clothesCommandService;

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Category parent = null;

        if (categoryRequest.getParentId() != null && categoryRequest.getParentId() > 0) {
            parent = categoryRepository.findByIdAndIsDeletedFalse(categoryRequest.getParentId()).orElseThrow(() -> {
                        log.warn("createCategory - 상위 카테고리 존재하지 않음. parentId={}", categoryRequest.getParentId());

                        return new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND);
                    }
            );
        }

        Category category = Category.create(categoryRequest.getName(), parent);
        categoryRepository.save(category);

        return CategoryResponse.from(category);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> {
                    log.warn("updateCategory - 수정할 카테고리 존재하지 않음. categoryId={}", id);

                    return new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND);
                }
        );

        Category parent = validateCategory(id, categoryRequest.getParentId());

        category.update(categoryRequest.getName(), parent);

        return CategoryResponse.from(category);
    }

    private Category validateCategory(Long categoryId, Long parentId) {

        if (parentId == null || parentId <= 0) {

            return null;
        }

        if (Objects.equals(categoryId, parentId)) {
            log.warn("validateCategory - 카테고리 자기 자신을 parent로 지정 시도. categoryId={}", categoryId);
            throw new CategoryException(CategoryErrorCode.CANNOT_SET_SELF_AS_PARENT);
        }

        Category parent = categoryRepository.findByIdAndIsDeletedFalse(parentId).orElseThrow(() -> {
                    log.warn("validateCategory - 상위 카테고리 존재하지 않음. parentId={}", parentId);

                    return new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND);
                }
        );

        validateCircularReference(categoryId, parent);

        return parent;
    }

    private void validateCircularReference(Long categoryId, Category parent) {

        Category current = parent.getParent();

        while (current != null) {
            if (Objects.equals(current.getId(), categoryId)) {
                log.warn("validateCircularReference - 순환 참조 발생. (검증대상 categoryId={}, 순환탐색 중 currentId={}, 최초 parentId={})",
                        categoryId,
                        current.getId(),
                        parent.getId()
                );
                throw new CategoryException(CategoryErrorCode.CATEGORY_CIRCULAR_REFERENCE);
            }
            current = current.getParent();
        }
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> {
                    log.warn("deleteCategory - 삭제할 카테고리 존재하지 않음. categoryId={}", id);

                    return new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND);
                }
        );

        List<Long> result = new ArrayList<>();
        result.add(id);

        List<Long> currentCategory = List.of(id);

        while (!currentCategory.isEmpty()) {
            List<Long> childCategory = categoryRepository.findIdsByParentIds(currentCategory);

            if (childCategory.isEmpty()) {
                break;
            }

            result.addAll(childCategory);
            currentCategory = childCategory;
        }

        if (!result.contains(id)) {
            result.add(id);
        }

        if (!result.isEmpty()) {
            clothesCommandService.clearCategoryFromClothes(result);
        }

        categoryRepository.softDeleteCategories(result);
    }
}