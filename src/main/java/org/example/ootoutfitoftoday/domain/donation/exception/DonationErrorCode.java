package org.example.ootoutfitoftoday.domain.donation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DonationErrorCode implements ErrorCode {

    INVALID_COORDINATES("INVALID_COORDINATES", HttpStatus.BAD_REQUEST, "유효하지 않은 좌표값입니다."),
    KAKAO_API_ERROR("KAKAO_API_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "카카오맵 API 호출 중 오류가 발생했습니다."),
    DONATION_CENTER_NOT_FOUND("DONATION_CENTER_NOT_FOUND", HttpStatus.NOT_FOUND, "기부처를 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
