package org.example.ootoutfitoftoday.domain.wearrecord.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class WearRecordException extends GlobalException {

    public WearRecordException(WearRecordErrorCode errorCode) {
        super(errorCode);
    }

}
