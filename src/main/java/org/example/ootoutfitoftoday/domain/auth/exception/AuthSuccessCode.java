package org.example.ootoutfitoftoday.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements SuccessCode {

    USER_SIGNUP("USER_SIGNUP", HttpStatus.CREATED, "회원가입이 완료되었습니다."),
    USER_LOGIN("USER_LOGIN", HttpStatus.OK, "로그인이 완료되었습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
