package org.example.ootoutfitoftoday.domain.payment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    // 결제 조회

    PAYMENT_NOT_FOUND(
            "PAYMENT_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            "결제 정보를 찾을 수 없습니다."
    ),

    // 결제 수단

    EASY_PAY_PROVIDER_REQUIRED(
            "EASY_PAY_PROVIDER_REQUIRED",
            HttpStatus.BAD_REQUEST,
            "간편결제 수단을 선택해주세요."
    ),

    // 결제 중복

    DUPLICATE_ORDER_ID(
            "DUPLICATE_ORDER_ID",
            HttpStatus.CONFLICT,
            "중복된 주문 ID입니다."
    ),

    ALREADY_APPROVED_PAYMENT(
            "ALREADY_APPROVED_PAYMENT",
            HttpStatus.CONFLICT,
            "이미 승인된 결제입니다."
    ),

    // 결제 검증

    PAYMENT_KEY_MISMATCH(
            "PAYMENT_KEY_MISMATCH",
            HttpStatus.BAD_REQUEST,
            "결제 키가 일치하지 않습니다."
    ),

    INVALID_PAYMENT_STATUS(
            "INVALID_PAYMENT_STATUS",
            HttpStatus.BAD_REQUEST,
            "승인 가능한 결제 상태가 아닙니다."
    ),

    //
    PAYMENT_CONFIRMATION_TIMEOUT(
            "PAYMENT_CONFIRMATION_TIMEOUT",
            HttpStatus.REQUEST_TIMEOUT,
            "결제 승인 시간이 초과되었습니다. (10분 이내 승인 필요)"
    ),

    // 토스 API

    TOSS_API_CLIENT_ERROR(
            "TOSS_API_CLIENT_ERROR",
            HttpStatus.BAD_REQUEST,
            "토스 결제 요청이 잘못되었습니다."
    ),

    TOSS_API_SERVER_ERROR(
            "TOSS_API_SERVER_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "토스 서버 오류가 발생했습니다."
    ),

    TOSS_API_TIMEOUT(
            "TOSS_API_TIMEOUT",
            HttpStatus.REQUEST_TIMEOUT,
            "토스 API 응답 시간이 초과되었습니다."
    ),

    TOSS_API_ERROR(
            "TOSS_API_ERROR",
            HttpStatus.BAD_REQUEST,
            "토스 결제 승인에 실패했습니다."
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
