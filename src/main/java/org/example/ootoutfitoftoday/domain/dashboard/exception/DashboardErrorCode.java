package org.example.ootoutfitoftoday.domain.dashboard.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DashboardErrorCode implements ErrorCode {

    DASHBOARD_ERROR_CODE("DASHBOARD_ERROR_CODE", HttpStatus.BAD_REQUEST, "대시보드 사용 중 예외가 발생하였습니다!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
