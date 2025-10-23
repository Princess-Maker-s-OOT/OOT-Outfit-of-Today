package org.example.ootoutfitoftoday.domain.clothes.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;

@Getter
public class ClothesColorCount {

    private final ClothesColor clothesColor;
    private final long count;

    @QueryProjection
    public ClothesColorCount(ClothesColor clothesColor, long count) {
        this.clothesColor = clothesColor;
        this.count = count;
    }
}
