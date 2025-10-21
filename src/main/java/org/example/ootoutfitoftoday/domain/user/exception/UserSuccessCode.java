package org.example.ootoutfitoftoday.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements SuccessCode {

    GET_MY_INFO("GET_MY_INFO", HttpStatus.OK, "회원정보 조회 완료입니다."),
    UPDATE_MY_INFO("UPDATE_MY_INFO", HttpStatus.OK, "회원정보 수정 완료입니다."),
    PASSWORD_VERIFIED("PASSWORD_VERIFIED", HttpStatus.OK, "인증이 완료되었습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
