package org.example.ootoutfitoftoday.domain.closet.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClosetErrorCode implements ErrorCode {

    CLOSET_NOT_FOUND("CLOSET_NOT_FOUND", HttpStatus.NOT_FOUND, "옷장을 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}