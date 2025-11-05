package org.example.ootoutfitoftoday.kakao.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum KakaoMapErrorCode implements ErrorCode {

    // API 호출 관련 에러
    API_CALL_FAILED("API_CALL_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "카카오맵 API 호출에 실패했습니다."),
    INVALID_API_KEY("INVALID_API_KEY", HttpStatus.UNAUTHORIZED, "카카오맵 API 키가 유효하지 않습니다."),
    API_QUOTA_EXCEEDED("API_QUOTA_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS, "카카오맵 API 호출 한도를 초과했습니다."),

    // 검색 결과 관련 에러
    NO_SEARCH_RESULTS("NO_SEARCH_RESULTS", HttpStatus.NOT_FOUND, "검색 결과가 없습니다."),
    INVALID_SEARCH_KEYWORD("INVALID_SEARCH_KEYWORD", HttpStatus.BAD_REQUEST, "검색 키워드가 유효하지 않습니다."),

    // 응답 처리 관련 에러
    INVALID_API_RESPONSE("INVALID_API_RESPONSE", HttpStatus.INTERNAL_SERVER_ERROR, "카카오맵 API 응답 형식이 올바르지 않습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}