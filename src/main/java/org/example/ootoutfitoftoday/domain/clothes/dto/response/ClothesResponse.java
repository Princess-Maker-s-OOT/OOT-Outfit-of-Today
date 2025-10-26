package org.example.ootoutfitoftoday.domain.clothes.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;

@Getter
@Builder
public class ClothesResponse {

    private final Long id;
    private final Long categoryId;
    private final Long userId;
    private final ClothesSize clothesSize;
    private final ClothesColor clothesColor;
    private final String description;

    @QueryProjection
    public ClothesResponse(
            Long id,
            Long categoryId,
            Long userId,
            ClothesSize clothesSize,
            ClothesColor clothesColor,
            String description
    ) {
        this.id = id;
        this.categoryId = categoryId;
        this.userId = userId;
        this.clothesSize = clothesSize;
        this.clothesColor = clothesColor;
        this.description = description;
    }

    public static ClothesResponse from(Clothes clothes) {

        return ClothesResponse.builder()
                .id(clothes.getId())
                .categoryId(
                        clothes.getCategory() != null
                                ? clothes.getCategory().getId()
                                : null
                )
                .userId(clothes.getUser().getId())
                .clothesSize(clothes.getClothesSize())
                .clothesColor(clothes.getClothesColor())
                .description(clothes.getDescription())
                .build();
    }
}
