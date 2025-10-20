package org.example.ootoutfitoftoday.domain.salepost.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class SalePostException extends GlobalException {

    public SalePostException(SalePostErrorCode errorCode) {
        super(errorCode);
    }

    public SalePostException(
            SalePostErrorCode errorCode,
            SalePostSuccessCode successCode
    ) {
        super(errorCode, successCode);
    }
}
