package org.example.ootoutfitoftoday.domain.transaction.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class TransactionException extends GlobalException {

    public TransactionException(TransactionErrorCode errorCode) {
        super(errorCode);
    }

    public TransactionException(
            TransactionErrorCode errorCode,
            TransactionSuccessCode successCode
    ) {
        super(errorCode, successCode);
    }
}
