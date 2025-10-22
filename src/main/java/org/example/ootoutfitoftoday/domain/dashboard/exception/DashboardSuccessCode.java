package org.example.ootoutfitoftoday.domain.dashboard.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DashboardSuccessCode implements SuccessCode {

    DASHBOARD_ADMIN_USER_STATISTICS_OK("DASHBOARD_ADMIN_USER_STATISTICS_OK", HttpStatus.OK, "사용자들에 대한 정보를 성공적으로 조회하였습니다!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
