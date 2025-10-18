package org.example.ootoutfitoftoday.domain.closet.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClosetErrorCode implements ErrorCode {

    CLOSET_UNAUTHORIZED("CLOSET_UNAUTHORIZED", HttpStatus.FORBIDDEN, "옷장에 접근할 권한이 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}