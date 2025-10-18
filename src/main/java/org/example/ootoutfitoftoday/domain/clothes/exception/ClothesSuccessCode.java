package org.example.ootoutfitoftoday.domain.clothes.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClothesSuccessCode implements SuccessCode {

    CLOTHES_CREATED("CLOTHES_CREATED", HttpStatus.CREATED, "옷을 등록하였습니다!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}


