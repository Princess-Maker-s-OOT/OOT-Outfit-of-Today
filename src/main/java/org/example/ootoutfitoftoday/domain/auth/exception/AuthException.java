package org.example.ootoutfitoftoday.domain.auth.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class AuthException extends GlobalException {

    public AuthException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }

    public AuthException(AuthErrorCode authErrorCode, AuthSuccessCode authSuccessCode) {
        super(authErrorCode, authSuccessCode);
    }
}
