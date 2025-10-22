package org.example.ootoutfitoftoday.domain.dashboard.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class DashboardException extends GlobalException {

    public DashboardException(DashboardErrorCode dashboardErrorCode) {
        super(dashboardErrorCode);
    }

    public DashboardException(DashboardErrorCode dashboardErrorCode, DashboardSuccessCode dashboardSuccessCode) {
        super(dashboardErrorCode, dashboardSuccessCode);
    }
}
