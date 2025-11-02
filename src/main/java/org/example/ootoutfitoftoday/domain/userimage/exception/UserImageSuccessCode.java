package org.example.ootoutfitoftoday.domain.userimage.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserImageSuccessCode implements SuccessCode {

    ;

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
