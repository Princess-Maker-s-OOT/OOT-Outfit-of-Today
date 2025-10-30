package org.example.ootoutfitoftoday.domain.clothesImage.dto.reponse;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;

@Getter
public class ClothesImageResponse {

    private final Long imageId;
    private final String imageUrl;
    private final Boolean isMain;

    @Builder
    public ClothesImageResponse(Long imageId, String imageUrl, Boolean isMain) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.isMain = isMain;
    }

    public static ClothesImageResponse from(ClothesImage clothesImage) {

        return ClothesImageResponse.builder()
                .imageId(clothesImage.getImage().getId())
                .imageUrl(clothesImage.getImage().getUrl())
                .isMain(clothesImage.getIsMain())
                .build();
    }
}
