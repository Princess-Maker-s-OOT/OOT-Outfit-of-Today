package org.example.ootoutfitoftoday.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    UNSUPPORTED_TOKEN("UNSUPPORTED_TOKEN", HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다."),
    MALFORMED_TOKEN("MALFORMED_TOKEN", HttpStatus.UNAUTHORIZED, "잘못된 JWT 토큰입니다."),
    MISSING_TOKEN("MISSING_TOKEN", HttpStatus.UNAUTHORIZED, "JWT 토큰이 필요합니다."),
    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.UNAUTHORIZED, "유효하지 않는 JWT 서명입니다."),
    EXPIRED_TOKEN("EXPIRED_TOKEN", HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    TOKEN_NOT_FOUND("TOKEN_NOT_FOUND", HttpStatus.NOT_FOUND, "토큰을 찾을 수 없습니다."),

    SERVER_ERROR("SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),

    AUTH_ANNOTATION_MISMATCH("AUTH_ANNOTATION_MISMATCH", HttpStatus.BAD_REQUEST, "@Auth와 AuthUser 타입은 함께 사용되어야 합니다."),

    CACHE_IS_NULL("CACHE_IS_NULL", HttpStatus.INTERNAL_SERVER_ERROR, "해당 캐시는 존재하지 않습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
