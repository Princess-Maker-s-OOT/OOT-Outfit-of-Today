package org.example.ootoutfitoftoday.domain.image.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageSuccessCode implements SuccessCode {

    PRESIGNED_URL_CREATED("PRESIGNED_URL_CREATED", HttpStatus.OK, "Presigned URL이 생성되었습니다."),
    IMAGE_SAVED("IMAGE_SAVED", HttpStatus.CREATED, "이미지가 저장되었습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}