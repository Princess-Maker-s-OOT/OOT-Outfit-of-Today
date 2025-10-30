package org.example.ootoutfitoftoday.domain.clothes.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryServiceImpl;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesErrorCode;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesException;
import org.example.ootoutfitoftoday.domain.clothes.repository.ClothesRepository;
import org.example.ootoutfitoftoday.domain.clothesImage.service.command.ClothesImageCommandService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ClothesCommandServiceImpl implements ClothesCommandService {

    private final ClothesRepository clothesRepository;
    private final CategoryQueryServiceImpl categoryQueryService;
    private final UserQueryService userQueryService;
    private final ClothesImageCommandService clothesImageCommandService;

    @Override
    public ClothesResponse createClothes(Long userId, ClothesRequest clothesRequest) {

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Category category = null;

        // 사용자가 categoryId를 입력한 경우에만 DB에서 조회
        if (clothesRequest.getCategoryId() != null) {
            category = categoryQueryService.findById(clothesRequest.getCategoryId());
        }

        Clothes clothes = Clothes.create(
                category,
                user,
                clothesRequest.getClothesSize(),
                clothesRequest.getClothesColor(),
                clothesRequest.getDescription(),
                new ArrayList<>()
        );

        Clothes savedClothes = clothesRepository.save(clothes);

        // 이미지를 저장할 경우 이미지 저장 로직을 ClothesImageCommandService로 위임
        if (clothesRequest.getImages() != null && !clothesRequest.getImages().isEmpty()) {

            clothesImageCommandService.saveClothesImages(savedClothes, clothesRequest.getImages());
        }

        return ClothesResponse.from(savedClothes);
    }

    // 아래는 이미지 삭제부터 구현 후 리팩토링 진행
    @Override
    public ClothesResponse updateClothes(
            Long userId,
            Long id,
            ClothesRequest clothesRequest
    ) {

        Clothes clothes = clothesRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND)
        );

        if (!Objects.equals(userId, clothes.getUser().getId())) {
            throw new ClothesException(ClothesErrorCode.CLOTHES_FORBIDDEN);
        }

        Category category = categoryQueryService.findById(clothesRequest.getCategoryId());

        clothes.update(
                category,
                clothesRequest.getClothesSize(),
                clothesRequest.getClothesColor(),
                clothesRequest.getDescription(),
                new ArrayList<>()
        );

        return ClothesResponse.from(clothes);
    }

    @Override
    public void deleteClothes(Long userId, Long id) {

        Clothes clothes = clothesRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND)
        );

        if (!Objects.equals(userId, clothes.getUser().getId())) {
            throw new ClothesException(ClothesErrorCode.CLOTHES_FORBIDDEN);
        }

        clothes.softDelete();
    }

    @Override
    public void clearCategoryFromClothes(List<Long> categoryIds) {
        clothesRepository.clearCategoryFromClothes(categoryIds);
    }

    @Override
    public void updateLastWornAt(Long clothesId, LocalDateTime wornAt) {
        Clothes clothes = clothesRepository.findByIdAndIsDeletedFalse(clothesId)
                .orElseThrow(() -> new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND));

        clothes.updateLastWornAt(wornAt);
    }
}
