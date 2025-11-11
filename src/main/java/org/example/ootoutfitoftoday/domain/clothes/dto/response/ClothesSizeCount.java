package org.example.ootoutfitoftoday.domain.clothes.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;

import java.io.Serializable;

@Getter
public class ClothesSizeCount implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ClothesSize clothesSize;
    private final long count;

    @JsonCreator
    @QueryProjection
    public ClothesSizeCount(
            @JsonProperty("clothesSize") ClothesSize clothesSize,
            @JsonProperty("count") long count
    ) {
        this.clothesSize = clothesSize;
        this.count = count;
    }
}
