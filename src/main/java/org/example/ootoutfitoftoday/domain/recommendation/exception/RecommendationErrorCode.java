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
    RECOMMENDATION_NOT_ACCEPTED("RECOMMENDATION_NOT_ACCEPTED", HttpStatus.BAD_REQUEST, "수락된 추천만 판매글을 작성할 수 있습니다."),
    RECOMMENDATION_NOT_SALE_TYPE("RECOMMENDATION_NOT_SALE_TYPE", HttpStatus.BAD_REQUEST, "판매 추천만 판매글을 작성할 수 있습니다."),
    RECOMMENDATION_NOT_DONATION_TYPE("RECOMMENDATION_NOT_DONATION_TYPE", HttpStatus.BAD_REQUEST, "기부 추천만 기부처를 검색할 수 있습니다."),
    SALE_POST_ALREADY_EXISTS("SALE_POST_ALREADY_EXISTS", HttpStatus.CONFLICT, "해당 추천에 대한 판매글이 이미 존재합니다."),
    RECOMMENDATION_PROCESSING_ERROR("RECOMMENDATION_PROCESSING_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "추천 기록 처리 중 알 수 없는 오류가 발생했습니다."),
    BATCH_HISTORY_NOT_FOUND("BATCH_HISTORY_NOT_FOUND", HttpStatus.NOT_FOUND, "배치 실행 이력을 찾을 수 없습니다."),
    USER_LOCATION_NOT_FOUND("USER_LOCATION_NOT_FOUND", HttpStatus.BAD_REQUEST, "사용자의 위치 정보를 찾을 수 없습니다."),
    INVALID_USER_LOCATION_FORMAT("INVALID_USER_LOCATION_FORMAT", HttpStatus.BAD_REQUEST, "사용자의 위치 정보 형식이 올바르지 않습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}