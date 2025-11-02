package org.example.ootoutfitoftoday.domain.userimage.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;
import org.example.ootoutfitoftoday.domain.user.exception.UserErrorCode;
import org.example.ootoutfitoftoday.domain.user.exception.UserSuccessCode;

public class UserImageException extends GlobalException {

    public UserImageException(UserImageErrorCode userImageErrorCode) {
        super(userImageErrorCode);
    }

    public UserImageException(UserErrorCode userErrorCode, UserSuccessCode userSuccessCode) {
        super(userErrorCode, userSuccessCode);
    }
}
