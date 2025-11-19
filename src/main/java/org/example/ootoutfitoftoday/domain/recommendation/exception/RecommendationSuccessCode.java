package org.example.ootoutfitoftoday.domain.recommendation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationSuccessCode implements SuccessCode {

    RECOMMENDATION_CREATED("RECOMMENDATION_CREATED", HttpStatus.CREATED, "추천 기록 생성 성공"),
    RECOMMENDATION_GET_OK("RECOMMENDATION_GET_OK", HttpStatus.OK, "추천 기록 조회 성공"),
    RECOMMENDATION_UPDATE_OK("RECOMMENDATION_UPDATE_OK", HttpStatus.OK, "추천 기록 상태 변경 성공"),
    SALE_POST_FROM_RECOMMENDATION_CREATED("SALE_POST_FROM_RECOMMENDATION_CREATED", HttpStatus.CREATED, "추천으로부터 판매글 생성 성공"),
    BATCH_HISTORY_GET_OK("BATCH_HISTORY_GET_OK", HttpStatus.OK, "배치 실행 이력 조회 성공"),
    DONATION_CENTER_SEARCH_FROM_RECOMMENDATION_OK("DONATION_CENTER_SEARCH_FROM_RECOMMENDATION_OK", HttpStatus.OK, "추천으로부터 기부처 검색 성공");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}