package org.example.ootoutfitoftoday.domain.transaction.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostSuccessCode;

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
