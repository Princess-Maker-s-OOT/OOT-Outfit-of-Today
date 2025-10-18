package org.example.ootoutfitoftoday.domain.closet.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class ClosetException extends GlobalException {
    public ClosetException(ClosetErrorCode errorCode) {
        super(errorCode);
    }

    public ClosetException(ClosetErrorCode errorCode, ClosetSuccessCode successCode) {
        super(errorCode, successCode);
    }
}