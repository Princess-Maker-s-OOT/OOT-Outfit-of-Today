package org.example.ootoutfitoftoday.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    USER_ALREADY_WITHDRAWN("USER_ALREADY_WITHDRAWN", HttpStatus.GONE, "이미 탈퇴한 사용자입니다."),
    PROFILE_IMAGE_NOT_FOUND("PROFILE_IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND, "존재하지 않는 프로필 이미지입니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
