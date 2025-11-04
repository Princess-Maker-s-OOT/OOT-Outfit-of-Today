package org.example.ootoutfitoftoday.domain.auth.service.command;

import jakarta.servlet.http.HttpServletRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthLoginRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthWithdrawRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.response.AuthLoginResponse;

public interface AuthCommandService {

    void initializeAdmin(
            String adminLoginId,
            String adminEmail,
            String adminNickname,
            String adminUsername,
            String adminPassword,
            String adminPhoneNumber);

    void signup(AuthSignupRequest request);

    // HttpServletRequest 파라미터 추가(IP, User-Agent 추출용)
    AuthLoginResponse login(AuthLoginRequest request, HttpServletRequest httpRequest);

    // deviceId 파라미터 추가 (디바이스 검증용)
    AuthLoginResponse refresh(String refreshToken, String deviceId);

    // deviceId 파라미터 추가(특정 디바이스만 로그아웃)
    void logout(AuthUser authUser, String deviceId);

    // 모든 디바이스에서 로그아웃
    void logoutAll(AuthUser authUser);

    // 특정 디바이스 강제 제거
    void removeDevice(AuthUser authUser, String deviceId);

    void withdraw(AuthWithdrawRequest request, AuthUser authUser);
}
