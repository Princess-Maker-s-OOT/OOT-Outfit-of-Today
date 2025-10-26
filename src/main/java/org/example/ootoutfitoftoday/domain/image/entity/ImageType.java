package org.example.ootoutfitoftoday.domain.image.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {
    CLOSET("closets", "옷장 이미지"),
    CLOTHES("clothes", "옷 이미지"),
    SALEPOST("saleposts", "판매글 이미지"),
    USER("users", "회원정보 이미지");

    private final String folder;  // S3 폴더명
    private final String description;

    /**
     * 문자열을 ImageType으로 변환
     *
     * @param type 이미지 타입 문자열
     * @return ImageType
     * @throws IllegalArgumentException 유효하지 않은 타입인 경우
     */
    public static ImageType fromString(String type) {
        try {

            return ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 이미지 타입입니다: " + type);
        }
    }
}