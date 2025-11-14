package org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response;

import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;

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