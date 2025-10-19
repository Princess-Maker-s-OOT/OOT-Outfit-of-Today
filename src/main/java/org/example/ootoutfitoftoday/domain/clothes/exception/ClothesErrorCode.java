package org.example.ootoutfitoftoday.domain.clothes.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClothesErrorCode implements ErrorCode {

    CLOTHES_NOT_FOUND("CLOTHES_NOT_FOUND", HttpStatus.NOT_FOUND, "옷을 찾을 수 없습니다!"),
    CLOTHES_FORBIDDEN("CLOTHES_FORBIDDEN", HttpStatus.FORBIDDEN, "사용자가 등록한 옷이 아닙니다!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
