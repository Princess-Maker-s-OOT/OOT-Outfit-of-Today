package org.example.ootoutfitoftoday.domain.transaction.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TransactionErrorCode implements ErrorCode {

    SALE_POST_NOT_AVAILABLE(
            "SALE_POST_NOT_AVAILABLE",
            HttpStatus.BAD_REQUEST,
            "판매 가능한 상태가 아닙니다."
    ),

    ALREADY_IN_TRANSACTION(
            "ALREADY_IN_TRANSACTION",
            HttpStatus.CONFLICT,
            "이미 진행 중인 거래가 있습니다."
    ),

    INVALID_PAYMENT_AMOUNT(
            "INVALID_PAYMENT_AMOUNT",
            HttpStatus.BAD_REQUEST,
            "결제 금액이 일치하지 않습니다."
    ),

    DUPLICATE_ORDER_ID(
            "DUPLICATE_ORDER_ID",
            HttpStatus.CONFLICT,
            "중복된 주문 ID입니다."
    ),

    CANNOT_BUY_OWN_POST(
            "CANNOT_BUY_OWN_POST",
            HttpStatus.BAD_REQUEST,
            "본인의 판매글은 구매할 수 없습니다."
    ),

    CHATROOM_REQUIRED_FOR_TRANSACTION(
            "CHATROOM_REQUIRED_FOR_TRANSACTION",
            HttpStatus.BAD_REQUEST,
            "거래를 시작하려면 판매자와 먼저 채팅을 시작해주세요."
    ),

    CHAT_REQUIRED_BEFORE_TRANSACTION(
            "CHAT_REQUIRED_BEFORE_TRANSACTION",
            HttpStatus.BAD_REQUEST,
            "거래를 시작하려면 판매자와 최소 1회 이상 대화가 필요합니다."
    ),

    EASY_PAY_PROVIDER_REQUIRED(
            "EASY_PAY_PROVIDER_REQUIRED",
            HttpStatus.BAD_REQUEST,
            "간편결제 수단을 선택해주세요."
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
