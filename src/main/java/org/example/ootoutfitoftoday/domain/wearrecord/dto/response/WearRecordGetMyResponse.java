package org.example.ootoutfitoftoday.domain.wearrecord.dto.response;

import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;
import org.example.ootoutfitoftoday.domain.wearrecord.entity.WearRecord;

import java.time.LocalDateTime;

public record WearRecordGetMyResponse(

        Long wearRecordId,
        LocalDateTime wornAt,
        Long clothesId,
        String clothesName,
        String clothesImageUrl
) {
    public static WearRecordGetMyResponse from(WearRecord wearRecord) {
        Clothes clothes = wearRecord.getClothes();

        String name = clothes.getDescription();

        String imageUrl = null;
        if (clothes.getImages() != null && !clothes.getImages().isEmpty()) {
            ClothesImage firstImage = clothes.getImages().get(0);
            if (firstImage != null) {
                // ClothesImage 엔티티를 거쳐 Image 엔티티의 getUrl()을 호출
                imageUrl = firstImage.getImage().getUrl();
            }
        }

        return new WearRecordGetMyResponse(
                wearRecord.getId(),
                wearRecord.getWornAt(),
                clothes.getId(),
                name,
                imageUrl
        );
    }
}