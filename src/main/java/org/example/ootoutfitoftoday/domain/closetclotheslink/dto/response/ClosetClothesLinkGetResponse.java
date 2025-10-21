package org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response;

import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;

public record ClosetClothesLinkGetResponse(

        Long linkId,
        Long clothesId,
        Long categoryId,
        ClothesSize clothesSize,
        ClothesColor clothesColor,
        String description
) {
    public static ClosetClothesLinkGetResponse from(ClosetClothesLink link) {

        return new ClosetClothesLinkGetResponse(
                link.getId(),
                link.getClothes().getId(),
                link.getClothes().getCategory() != null
                        ? link.getClothes().getCategory().getId()
                        : null,
                link.getClothes().getClothesSize(),
                link.getClothes().getClothesColor(),
                link.getClothes().getDescription()
        );
    }
}