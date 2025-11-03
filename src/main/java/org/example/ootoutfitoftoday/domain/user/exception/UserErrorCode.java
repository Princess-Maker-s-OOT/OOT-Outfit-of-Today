package org.example.ootoutfitoftoday.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
