package org.example.ootoutfitoftoday.domain.clothes.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;

@Getter
@Builder
@RequiredArgsConstructor
public class ClothesResponse {

    private final Long id;
    private final Long categoryId;
    private final Long userId;
    private final ClothesSize clothesSize;
    private final ClothesColor clothesColor;
    private final String description;

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
