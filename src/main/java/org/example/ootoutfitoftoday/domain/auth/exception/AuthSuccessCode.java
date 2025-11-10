package org.example.ootoutfitoftoday.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements SuccessCode {

    USER_SIGNUP("USER_SIGNUP", HttpStatus.CREATED, "회원가입이 완료되었습니다."),
    USER_LOGIN("USER_LOGIN", HttpStatus.OK, "로그인이 완료되었습니다."),
    USER_WITHDRAW("USER_WITHDRAW", HttpStatus.OK, "회원탈퇴가 완료되었습니다."),

    TOKEN_REFRESH("TOKEN_REFRESH", HttpStatus.OK, "토큰이 재발급되었습니다."),
    USER_LOGOUT("USER_LOGOUT", HttpStatus.OK, "로그아웃이 완료되었습니다."),
    TOKEN_EXCHANGE("TOKEN_EXCHANGE", HttpStatus.OK, "토큰 교환이 완료되었습니다"),

    DEVICE_LIST_RETRIEVED("DEVICE_LIST_RETRIEVED", HttpStatus.OK, "디바이스 목록 조회가 완료되었습니다."),
    DEVICE_REMOVED("DEVICE_REMOVED", HttpStatus.OK, "디바이스가 제거되었습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
