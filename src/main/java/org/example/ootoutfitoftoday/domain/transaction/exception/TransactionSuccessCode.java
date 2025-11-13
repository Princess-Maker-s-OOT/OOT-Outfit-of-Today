package org.example.ootoutfitoftoday.domain.transaction.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TransactionSuccessCode implements SuccessCode {

    TRANSACTION_REQUESTED(
            "TRANSACTION_REQUESTED",
            HttpStatus.CREATED,
            "거래가 요청되었습니다."
    ),

    TRANSACTION_ACCEPTED(
            "TRANSACTION_ACCEPTED",
            HttpStatus.OK,
            "거래가 수락되었습니다."
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
