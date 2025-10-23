package org.example.ootoutfitoftoday.domain.clothes.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;

@Getter
public class ClothesSizeCount {

    private final ClothesSize clothesSize;
    private final long count;

    @QueryProjection
    public ClothesSizeCount(ClothesSize clothesSize, long count) {
        this.clothesSize = clothesSize;
        this.count = count;
    }
}
