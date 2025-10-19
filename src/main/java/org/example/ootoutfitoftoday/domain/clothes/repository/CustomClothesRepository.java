package org.example.ootoutfitoftoday.domain.clothes.repository;

import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.springframework.data.domain.Page;

public interface CustomClothesRepository {

    Page<Clothes> findAllByIsDeletedFalse(
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            int page,
            int size,
            String sort,
            String direction
    );
}
