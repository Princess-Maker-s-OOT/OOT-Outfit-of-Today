package org.example.ootoutfitoftoday.domain.image.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class ImageException extends GlobalException {
    public ImageException(ImageErrorCode errorCode) {
        super(errorCode);
    }

    public ImageException(ImageErrorCode errorCode, ImageSuccessCode successCode) {
        super(errorCode, successCode);
    }
}