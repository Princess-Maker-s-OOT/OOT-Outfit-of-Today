package org.example.ootoutfitoftoday.domain.user.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class UserException extends GlobalException {

    public UserException(UserErrorCode userErrorCode) {
        super(userErrorCode);
    }

    public UserException(UserErrorCode userErrorCode, UserSuccessCode userSuccessCode) {
        super(userErrorCode, userSuccessCode);
    }
}
