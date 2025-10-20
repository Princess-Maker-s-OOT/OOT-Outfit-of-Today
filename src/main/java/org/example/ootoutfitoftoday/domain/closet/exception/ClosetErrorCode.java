package org.example.ootoutfitoftoday.domain.closet.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClosetErrorCode implements ErrorCode {

    CLOSET_NOT_FOUND("CLOSET_NOT_FOUND", HttpStatus.NOT_FOUND, "옷장을 찾을 수 없습니다."),
    CLOSET_DELETED("CLOSET_DELETED", HttpStatus.NOT_FOUND, "삭제된 옷장입니다."),
    CLOSET_FORBIDDEN("CLOSET_FORBIDDEN", HttpStatus.FORBIDDEN, "해당 옷장에 대한 권한이 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}