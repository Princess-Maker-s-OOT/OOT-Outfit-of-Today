package org.example.ootoutfitoftoday.domain.wearrecord.dto.response;

import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
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
            imageUrl = clothes.getImages().stream()
                    .filter(image -> Boolean.TRUE.equals(image.getIsMain()))
                    .findFirst()
                    .map(mainImage -> mainImage.getImage().getUrl()) // URL로 변환
                    .orElse(clothes.getImages().get(0).getImage().getUrl());
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