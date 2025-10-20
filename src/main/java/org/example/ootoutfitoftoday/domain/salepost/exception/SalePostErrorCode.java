package org.example.ootoutfitoftoday.domain.salepost.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SalePostErrorCode implements ErrorCode {

    // === 도메인 검증 (엔티티 내부) ===
    INVALID_PRICE("INVALID_PRICE", HttpStatus.BAD_REQUEST, "가격은 0보다 커야 합니다."),
    EMPTY_IMAGES("EMPTY_IMAGES", HttpStatus.BAD_REQUEST, "이미지는 최소 1개 이상 필요합니다."),
    EMPTY_IMAGE_URL("EMPTY_IMAGE_URL", HttpStatus.BAD_REQUEST, "이미지 URL은 필수입니다."),
//    INVALID_DISPLAY_ORDER("INVALID_DISPLAY_ORDER", HttpStatus.BAD_REQUEST, "이미지 순서값이 유효하지 않습니다."),


    // === 비즈니스 검증 (서비스 레이어) ===
    SALE_POST_NOT_FOUND("SALE_POST_NOT_FOUND", HttpStatus.NOT_FOUND, "판매글을 찾을 수 없습니다.");


    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
