package org.example.ootoutfitoftoday.domain.donation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DonationSuccessCode implements SuccessCode {

    DONATION_CENTER_SEARCH_SUCCESS("DONATION_CENTER_SEARCH_SUCCESS", HttpStatus.OK, "주변 기부처 검색에 성공했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
