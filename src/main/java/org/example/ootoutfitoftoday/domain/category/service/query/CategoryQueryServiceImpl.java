package org.example.ootoutfitoftoday.domain.category.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.repository.CategoryRepository;
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
}
