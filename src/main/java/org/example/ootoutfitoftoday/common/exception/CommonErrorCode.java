package org.example.ootoutfitoftoday.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // ======= 현재 사용 중 ======
    UNEXPECTED_SERVER_ERROR("UNEXPECTED_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "예상하지 못한 서버 오류가 발생했습니다."),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation 오류가 발생했습니다."),
    // ========================

    CACHE_IS_NULL("CACHE_IS_NULL", HttpStatus.INTERNAL_SERVER_ERROR, "해당 캐시는 존재하지 않습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
