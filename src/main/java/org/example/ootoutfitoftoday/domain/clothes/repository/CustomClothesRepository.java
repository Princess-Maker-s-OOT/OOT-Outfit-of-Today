package org.example.ootoutfitoftoday.domain.clothes.repository;

import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesColorCount;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesSizeCount;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CustomClothesRepository {

    Page<Clothes> findAllByIsDeletedFalse(
            Long categoryId,
            Long userId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            int page,
            int size,
            String sort,
            String direction
    );

    // 최상위 카테고리 기준 옷 통계
    List<CategoryStat> countTopCategoryStats();

    // 색상 기준 옷 통계
    List<ClothesColorCount> clothesColorsCount();

    // 사이즈 기준 옷 통계
    List<ClothesSizeCount> clothesSizesCount();

    List<CategoryStat> findTopCategoryStats();
}
