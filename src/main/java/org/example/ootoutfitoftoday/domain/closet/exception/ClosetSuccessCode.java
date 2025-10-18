package org.example.ootoutfitoftoday.domain.closet.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClosetSuccessCode implements SuccessCode {

    CLOSET_CREATED("CLOSET_CREATED", HttpStatus.CREATED, "옷장이 등록되었습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}