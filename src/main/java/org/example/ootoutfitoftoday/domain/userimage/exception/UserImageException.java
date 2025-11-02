package org.example.ootoutfitoftoday.domain.userimage.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class UserImageException extends GlobalException {

    public UserImageException(UserImageErrorCode userImageErrorCode) {
        super(userImageErrorCode);
    }

    public UserImageException(UserImageErrorCode userImageErrorCode, UserImageSuccessCode userImageSuccessCode) {
        super(userImageErrorCode, userImageSuccessCode);
    }
}
