package org.example.ootoutfitoftoday.domain.auth.service.command;

import jakarta.servlet.http.HttpServletRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthLoginRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthWithdrawRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.response.AuthLoginResponse;

public interface AuthCommandService {

    void signup(AuthSignupRequest request);

    AuthLoginResponse login(AuthLoginRequest request, HttpServletRequest httpRequest);

    AuthLoginResponse refresh(
            String refreshToken,
            String deviceId,
            HttpServletRequest httpRequest);

    AuthLoginResponse exchangeOAuthToken(
            String code,
            String deviceId,
            String deviceName,
            HttpServletRequest httpRequest);

    void logout(AuthUser authUser, String deviceId);

    void logoutAll(AuthUser authUser);

    void removeDevice(
            AuthUser authUser,
            String deviceId,
            String currentDeviceId);

    void withdraw(AuthWithdrawRequest request, AuthUser authUser);
}
