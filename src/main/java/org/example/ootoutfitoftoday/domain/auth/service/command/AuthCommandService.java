package org.example.ootoutfitoftoday.domain.auth.service.command;

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

    AuthLoginResponse login(AuthLoginRequest request);

    AuthLoginResponse refresh(String refreshToken);

    void withdraw(AuthWithdrawRequest request, AuthUser authUser);
}
