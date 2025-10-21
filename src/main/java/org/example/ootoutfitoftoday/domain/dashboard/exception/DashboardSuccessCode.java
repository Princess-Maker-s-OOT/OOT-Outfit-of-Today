package org.example.ootoutfitoftoday.domain.dashboard.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DashboardSuccessCode implements SuccessCode {

    DASHBOARD_SUCCESS_CODE("DASHBOARD_SUCCESS_CODE", HttpStatus.OK, "대시보드를 성공적으로 조회하였습니다!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
