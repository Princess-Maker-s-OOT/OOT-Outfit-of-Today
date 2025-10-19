package org.example.ootoutfitoftoday.domain.clothes.repository;

import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomClothesRepository {

    Page<Clothes> findAllByIsDeletedFalse(
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            Pageable pageable
    );
}
