package org.example.ootoutfitoftoday.kakao.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class KakaoMapException extends GlobalException {

    public KakaoMapException(KakaoMapErrorCode errorCode) {
        super(errorCode);
    }
}