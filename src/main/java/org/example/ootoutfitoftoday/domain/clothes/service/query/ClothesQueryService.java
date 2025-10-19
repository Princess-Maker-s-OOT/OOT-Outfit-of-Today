package org.example.ootoutfitoftoday.domain.clothes.service.query;

import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.springframework.data.domain.Page;

public interface ClothesQueryService {

    Page<ClothesResponse> getClothes(
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            int page,
            int size,
            String sort,
            String direction
    );
}
