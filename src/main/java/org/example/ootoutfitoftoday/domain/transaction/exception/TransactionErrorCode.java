package org.example.ootoutfitoftoday.domain.transaction.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TransactionErrorCode implements ErrorCode {

    // 거래 조건 검증

    SALE_POST_NOT_AVAILABLE(
            "SALE_POST_NOT_AVAILABLE",
            HttpStatus.BAD_REQUEST,
            "판매 가능한 상태가 아닙니다."
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

    // 거래 상태/중복

    ALREADY_IN_TRANSACTION(
            "ALREADY_IN_TRANSACTION",
            HttpStatus.CONFLICT,
            "이미 진행 중인 거래가 있습니다."
    ),

    TRANSACTION_NOT_FOUND(
            "TRANSACTION_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            "거래를 찾을 수 없습니다."
    ),

    UNAUTHORIZED_TRANSACTION_ACCESS(
            "UNAUTHORIZED_TRANSACTION_ACCESS",
            HttpStatus.FORBIDDEN,
            "해당 거래에 대한 권한이 없습니다."
    ),

    INVALID_TRANSACTION_STATUS(
            "INVALID_TRANSACTION_STATUS",
            HttpStatus.BAD_REQUEST,
            "승인 가능한 거래 상태가 아닙니다."
    ),

    TRANSACTION_NOT_CANCELLABLE(
            "TRANSACTION_NOT_CANCELLABLE",
            HttpStatus.BAD_REQUEST,
            "판매자 수락 전의 거래만 취소할 수 있습니다."
    ),

    // 금액 검증

    INVALID_PAYMENT_AMOUNT(
            "INVALID_PAYMENT_AMOUNT",
            HttpStatus.BAD_REQUEST,
            "결제 금액이 일치하지 않습니다."
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
