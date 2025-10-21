package org.example.ootoutfitoftoday.domain.clothes.service.command;

import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;

import java.util.List;

public interface ClothesCommandService {

    ClothesResponse createClothes(Long userId, ClothesRequest clothesRequest);

    ClothesResponse updateClothes(
            Long userId,
            Long id,
            ClothesRequest clothesRequest
    );

    void deleteClothes(Long userId, Long id);

    void clearCategoryFromClothes(List<Long> categoryIds);
}
