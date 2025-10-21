package org.example.ootoutfitoftoday.domain.closetclotheslink.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClosetClothesLinkSuccessCode implements SuccessCode {

    CLOSET_CLOTHES_LINKED("CLOSET_CLOTHES_LINKED", HttpStatus.CREATED, "옷장에 옷이 등록되었습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
