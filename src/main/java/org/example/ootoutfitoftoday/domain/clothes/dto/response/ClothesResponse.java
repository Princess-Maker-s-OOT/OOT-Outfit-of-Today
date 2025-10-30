package org.example.ootoutfitoftoday.domain.clothes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.reponse.ClothesImageResponse;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class ClothesResponse {

    private final Long id;
    private final Long categoryId;
    private final Long userId;
    private final ClothesSize clothesSize;
    private final ClothesColor clothesColor;
    private final String description;
    private final List<ClothesImageResponse> clothesImages;

    public static ClothesResponse from(Clothes clothes) {

        List<ClothesImageResponse> imageResponses = clothes.getImages().stream()
                .map(ClothesImageResponse::from)
                .collect(Collectors.toList());

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
                .clothesImages(imageResponses)
                .build();
    }
}
