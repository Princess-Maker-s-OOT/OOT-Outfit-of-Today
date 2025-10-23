package org.example.ootoutfitoftoday.domain.clothes.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;

@Getter
public class CountClothesColor {

    private final ClothesColor clothesColor;
    private final long count;

    @QueryProjection
    public CountClothesColor(ClothesColor clothesColor, long count) {
        this.clothesColor = clothesColor;
        this.count = count;
    }
}
