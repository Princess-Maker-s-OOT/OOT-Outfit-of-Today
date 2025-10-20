package org.example.ootoutfitoftoday.domain.category.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryResponse;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Optional<Category> findById(long id) {

        return categoryRepository.findById(id);
    }

    @Override
    public Page<CategoryResponse> getCategories(
            int page,
            int size,
            String sort,
            String direction
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.fromString(direction), // asc or desc
                        sort // 정렬 기준 필드명
                )
        );

        Page<Category> categories = categoryRepository.findAllByIsDeletedFalse(pageable);

        return categories.map(CategoryResponse::from);
    }
}
