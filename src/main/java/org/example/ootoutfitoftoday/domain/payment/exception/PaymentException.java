package org.example.ootoutfitoftoday.domain.payment.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class PaymentException extends GlobalException {

    public PaymentException(PaymentErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentException(
            PaymentErrorCode errorCode,
            PaymentSuccessCode successCode
    ) {
        super(errorCode, successCode);
    }
}
