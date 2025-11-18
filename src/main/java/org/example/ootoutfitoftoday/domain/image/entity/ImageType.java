package org.example.ootoutfitoftoday.domain.image.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.image.exception.ImageErrorCode;
import org.example.ootoutfitoftoday.domain.image.exception.ImageException;

@Getter
@RequiredArgsConstructor
public enum ImageType {
    CLOSET("closets", "옷장 이미지"),
    CLOTHES("clothes", "옷 이미지"),
    SALEPOST("saleposts", "판매글 이미지"),
    USER("users", "회원정보 이미지");

    private final String folder;
    private final String description;

    public static ImageType fromString(String type) {
        try {
            return ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ImageException(ImageErrorCode.INVALID_IMAGE_TYPE);
        }
    }
}