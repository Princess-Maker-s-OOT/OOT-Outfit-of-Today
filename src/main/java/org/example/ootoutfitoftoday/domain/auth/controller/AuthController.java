package org.example.ootoutfitoftoday.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthLoginRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthWithdrawRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.response.AuthLoginResponse;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthSuccessCode;
import org.example.ootoutfitoftoday.domain.auth.service.command.AuthCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthCommandService authCommandService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody AuthSignupRequest request
    ) {
        authCommandService.signup(request);

        return ApiResponse.success(null, AuthSuccessCode.USER_SIGNUP);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthLoginResponse>> login(
            @Valid @RequestBody AuthLoginRequest request
    ) {
        AuthLoginResponse response = authCommandService.login(request);

        return ApiResponse.success(response, AuthSuccessCode.USER_LOGIN);
    }

    // 회원탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @Valid @RequestBody AuthWithdrawRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        authCommandService.withdraw(request, authUser);

        return ApiResponse.success(null, AuthSuccessCode.USER_WITHDRAW);
    }
}
