package org.example.ootoutfitoftoday.domain.clothes.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.exception.CategoryErrorCode;
import org.example.ootoutfitoftoday.domain.category.exception.CategoryException;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryService;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.repository.ClothesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ClothesCommandServiceImpl implements ClothesCommandService {

    private final ClothesRepository clothesRepository;
    private final CategoryQueryService categoryQueryService;

    @Override
    public ClothesResponse createClothes(ClothesRequest clothesRequest) {

//        User user = clothesRepository.findById(userId).orElseThrow(
//                () -> new UserException(UserErrorCode.USER_NOT_FOUND)
//        );

        Category category = null;

        // 사용자가 categoryId를 입력한 경우에만 DB에서 조회
        if (clothesRequest.getCategoryId() != null) {
            category = categoryQueryService.findById(clothesRequest.getCategoryId())
                    .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND)
                    );
        }

        Clothes clothes = Clothes.create(
                category,
                clothesRequest.getClothesSize(),
                clothesRequest.getClothesColor(),
                clothesRequest.getDescription()
        );

        clothesRepository.save(clothes);

        return ClothesResponse.from(clothes);
    }
}
