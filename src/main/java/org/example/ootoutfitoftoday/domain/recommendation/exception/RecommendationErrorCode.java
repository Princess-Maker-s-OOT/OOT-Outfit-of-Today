package org.example.ootoutfitoftoday.domain.recommendation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationErrorCode implements ErrorCode {

    INVALID_RECOMMENDATION_TYPE("INVALID_RECOMMENDATION_TYPE", HttpStatus.BAD_REQUEST, "잘못된 추천 타입입니다."),
    INVALID_STATUS_UPDATE("INVALID_STATUS_UPDATE", HttpStatus.BAD_REQUEST, "현재 상태에서 변경할 수 없는 상태입니다."),
    RECOMMENDATION_NOT_FOUND("RECOMMENDATION_NOT_FOUND", HttpStatus.NOT_FOUND, "해당 추천 기록을 찾을 수 없습니다."),
    RECOMMENDATION_PROCESSING_ERROR("RECOMMENDATION_PROCESSING_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "추천 기록 처리 중 알 수 없는 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}