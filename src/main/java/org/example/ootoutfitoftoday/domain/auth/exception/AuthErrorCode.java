package org.example.ootoutfitoftoday.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    DUPLICATE_LOGIN_ID("DUPLICATE_LOGIN_ID", HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME("DUPLICATE_NICKNAME", HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    DUPLICATE_PHONE_NUMBER("DUPLICATE_PHONE_NUMBER", HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다."),

    INVALID_LOGIN_CREDENTIALS("INVALID_LOGIN_CREDENTIALS", HttpStatus.UNAUTHORIZED, "잘못된 아이디 또는 비밀번호입니다."),
    INVALID_PASSWORD("INVALID_PASSWORD", HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "비밀번호는 필수 입력값입니다."),

    EXPIRED_REFRESH_TOKEN("EXPIRED_REFRESH_TOKEN", HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND("REFRESH_TOKEN_NOT_FOUND", HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),
    INVALID_TOKEN_TYPE("INVALID_TOKEN_TYPE", HttpStatus.BAD_REQUEST, "잘못된 토큰 타입입니다."),
    OAUTH_LOGIN_FAILED("OAUTH_LOGIN_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "임시 코드 생성 실패입니다"),
    TOKEN_EXCHANGE_FAILED("TOKEN_EXCHANGE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "토큰 교환에 실패했습니다"),
    INVALID_OR_EXPIRED_CODE("INVALID_OR_EXPIRED_CODE", HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 임시 코드입니다"),

    TOKEN_SERIALIZATION_FAILED("TOKEN_SERIALIZATION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "토큰 정보 직렬화에 실패했습니다"),
    REDIS_CONNECTION_FAILED("REDIS_CONNECTION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "Redis 연결에 실패했습니다"),
    REDIS_SAVE_FAILED("REDIS_SAVE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "Redis 저장에 실패했습니다"),

    DEVICE_MISMATCH("DEVICE_MISMATCH", HttpStatus.UNAUTHORIZED, "디바이스 정보가 일치하지 않습니다."),
    DEVICE_NOT_FOUND("DEVICE_NOT_FOUND", HttpStatus.NOT_FOUND, "디바이스 정보를 찾을 수 없습니다."),
    INVALID_DEVICE("INVALID_DEVICE", HttpStatus.BAD_REQUEST, "유효하지 않은 디바이스 ID입니다."),
    CANNOT_REMOVE_CURRENT_DEVICE("CANNOT_REMOVE_CURRENT_DEVICE", HttpStatus.BAD_REQUEST, "현재 사용 중인 디바이스는 제거할 수 없습니다. 로그아웃을 사용해주세요."),

    ACCOUNT_ALREADY_LINKED("ACCOUNT_ALREADY_LINKED", HttpStatus.BAD_REQUEST, "이미 다른 계정에 연결된 이메일입니다."),

    INVALID_OAUTH2_TOKEN("INVALID_OAUTH2_TOKEN", HttpStatus.INTERNAL_SERVER_ERROR, "유효하지 않은 OAuth2 인증 토큰입니다."),

    USER_ALREADY_WITHDRAWN("USER_ALREADY_WITHDRAWN", HttpStatus.GONE, "이미 탈퇴한 사용자입니다."),

    CONCURRENT_LOGIN_IN_PROGRESS("CONCURRENT_LOGIN_IN_PROGRESS", HttpStatus.CONFLICT, "동시 로그인 처리 중입니다. 잠시 후 다시 시도해주세요."),
    LOGOUT_IN_PROGRESS("LOGOUT_IN_PROGRESS", HttpStatus.CONFLICT, "로그아웃 처리 중입니다. 잠시 후 다시 시도해주세요."),
    DEVICE_REMOVAL_IN_PROGRESS("DEVICE_REMOVAL_IN_PROGRESS", HttpStatus.CONFLICT, "디바이스 제거 처리 중입니다. 잠시 후 다시 시도해주세요."),
    WITHDRAWAL_IN_PROGRESS("WITHDRAWAL_IN_PROGRESS", HttpStatus.CONFLICT, "회원탈퇴 처리 중입니다. 잠시 후 다시 시도해주세요.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
