package org.example.ootoutfitoftoday.domain.chat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

    NO_CHAT("NO_CHAT", HttpStatus.BAD_REQUEST, "해당 채팅이 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
