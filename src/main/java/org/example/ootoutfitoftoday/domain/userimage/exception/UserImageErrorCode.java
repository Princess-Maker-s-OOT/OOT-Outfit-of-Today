package org.example.ootoutfitoftoday.domain.userimage.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserImageErrorCode implements ErrorCode {

    USER_IMAGE_NOT_FOUND("USER_IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND, "사용자 이미지를 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
