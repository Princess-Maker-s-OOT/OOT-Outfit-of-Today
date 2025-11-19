package org.example.ootoutfitoftoday.domain.clothes.repository;

import com.ootcommon.category.response.CategoryStat;
import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import com.ootcommon.clothes.response.ClothesColorCount;
import com.ootcommon.clothes.response.ClothesSizeCount;
import com.ootcommon.wearrecord.response.ClothesWearCount;
import com.ootcommon.wearrecord.response.NotWornOverPeriod;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ClothesCustomRepository {

    Slice<Clothes> findAllByIsDeletedFalse(
            Long userId,
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            Long lastClothesId,
            int size
    );

    List<CategoryStat> countTopCategoryStats();

    List<ClothesColorCount> clothesColorsCount();

    List<ClothesSizeCount> clothesSizesCount();

    List<CategoryStat> findTopCategoryStats();

    List<ClothesWearCount> leastWornClothes(Long userId);

    List<NotWornOverPeriod> notWornOverPeriod(Long userId);
}
