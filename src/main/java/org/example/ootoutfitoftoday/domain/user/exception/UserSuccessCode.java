package org.example.ootoutfitoftoday.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements SuccessCode {

    USER_SUCCESS_CODE("USER_SUCCESS_CODE",HttpStatus.OK, "임시 성공 코드입니다!"),;

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
