package org.example.ootoutfitoftoday.domain.clothes.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;

import java.io.Serializable;

@Getter
public class ClothesColorCount implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ClothesColor clothesColor;
    private final long count;

    @JsonCreator
    @QueryProjection
    public ClothesColorCount(
            @JsonProperty("clothesColor") ClothesColor clothesColor,
            @JsonProperty("count") long count
    ) {
        this.clothesColor = clothesColor;
        this.count = count;
    }
}
