package org.example.ootoutfitoftoday.domain.closetclotheslink.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClosetClothesLinkErrorCode implements ErrorCode {

    CLOSET_CLOTHES_ALREADY_LINKED("CLOTHES_ALREADY_LINKED", HttpStatus.BAD_REQUEST, "이미 옷장에 등록된 옷입니다."),
    CLOSET_CLOTHES_FORBIDDEN("CLOSET_CLOTHES_FORBIDDEN", HttpStatus.FORBIDDEN, "해당 옷장에 대한 권한이 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
