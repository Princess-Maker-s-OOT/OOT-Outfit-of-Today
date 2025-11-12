package org.example.ootoutfitoftoday.domain.category.service.command;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandServiceImpl implements CategoryCommandService {

    private final CategoryRepository categoryRepository;
    private final ClothesCommandService clothesCommandService;

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {

        Category parent = null;

        /**
         *  상위 카테고리를 입력했다면 존재 여부를 검증
         *  - null 뿐만 아니라 0도 조건으로 건 이유는 아이디의 값이 1부터 시작하기 때문이다.
         *  - 추가로 사용자가 아이디의 값을 0 이하로 입력시 아이디의 값이 null로 처리되어 최상위 카테고리로 인식한다.
         */
        if (categoryRequest.getParentId() != null && categoryRequest.getParentId() > 0) {
            parent = categoryRepository.findByIdAndIsDeletedFalse(categoryRequest.getParentId())
                    .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND)
                    );
        }

        Category category = Category.create(categoryRequest.getName(), parent);
        categoryRepository.save(category);

        return CategoryResponse.from(category);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {

        // 수정할 카테고리가 존재하는 지 검증
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND)
                );

        // 새로 설정할 부모 카테고리를 검증하고 조회
        Category parent = validateCategory(id, categoryRequest.getParentId());

        // 카테고리명과 새로 설정할 상위 카테고리로 데이터 수정
        category.update(categoryRequest.getName(), parent);

        return CategoryResponse.from(category);
    }

    // 부모 카테고리 검증 로직
    private Category validateCategory(Long categoryId, Long parentId) {

        // 상위 카테고리가 null 이거나 0 이하면 최상위 카테고리
        if (parentId == null || parentId <= 0) {

            return null;
        }

        // 자기 자신을 부모로 설정할 경우
        if (Objects.equals(categoryId, parentId)) {
            throw new CategoryException(CategoryErrorCode.CANNOT_SET_SELF_AS_PARENT);
        }

        // 부모 카테고리 조회 (설정하려고 하는 상위 카테고리가 존재하는 지 검증)
        Category parent = categoryRepository.findByIdAndIsDeletedFalse(parentId)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        // 순환 참조 검증
        validateCircularReference(categoryId, parent);

        return parent;
    }

    // 순환 참조 검증 로직 (categoryId: 수정하려고 하는 카테고리 아이디, parent: 새로 지정하려는 상위 카테고리 객체)
    private void validateCircularReference(Long categoryId, Category parent) {

        // 이미 위에서 수정할 카테고리 아이디와 상위의 아이디 값이 같은 지 검증했음. 그래서 parent의 상위부터 탐색 시작
        Category current = parent.getParent();

        // 최상위 카테고리까지 반복 (상향 탐색 방식)
        while (current != null) {

            // current의 상위들 중에 자신이 있다면 예외
            if (Objects.equals(current.getId(), categoryId)) {
                throw new CategoryException(CategoryErrorCode.CATEGORY_CIRCULAR_REFERENCE);
            }

            current = current.getParent();
        }
    }

    @Override
    public void deleteCategory(Long id) {

        categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND)
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

        // 삭제 대상 카테고리가 누락된 경우 대비
        if (!result.contains(id)) {
            result.add(id);
        }

        if (!result.isEmpty()) {
            clothesCommandService.clearCategoryFromClothes(result);
        }

        categoryRepository.softDeleteCategories(result);
    }
}
