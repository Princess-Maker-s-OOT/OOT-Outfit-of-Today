package org.example.ootoutfitoftoday.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthLoginRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthWithdrawRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.RefreshTokenRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.response.AuthLoginResponse;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthSuccessCode;
import org.example.ootoutfitoftoday.domain.auth.service.command.AuthCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 관리", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthCommandService authCommandService;

    // 회원가입
    @Operation(
            summary = "회원 생성",
            description = "새로운 회원을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "409", description = "중복 요청")
            })
    @PostMapping("/signup")
    public ResponseEntity<Response<Void>> signup(
            @Valid @RequestBody AuthSignupRequest request
    ) {
        authCommandService.signup(request);

        return Response.success(null, AuthSuccessCode.USER_SIGNUP);
    }

    // 로그인
    @Operation(
            summary = "회원 로그인",
            description = "아이디와 비밀번호를 사용하여 로그인합니다.\n\n" +
                    "- Access Token: 응답 바디에 포함 (60분)\n" +
                    "- Refresh Token: 응답 바디에 포함 (7일)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 생성"),
                    @ApiResponse(responseCode = "401", description = "로그인 실패(잘못된 아이디 또는 비밀번호)")
            })
    @PostMapping("/login")
    public ResponseEntity<Response<AuthLoginResponse>> login(
            @Valid @RequestBody AuthLoginRequest request
    ) {
        AuthLoginResponse response = authCommandService.login(request);

        return Response.success(response, AuthSuccessCode.USER_LOGIN);
    }

    // 토큰 재발급
    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.(RTR)\n\n" +
                    "- 리프레시 토큰은 Body로 전송",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
                    @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰")
            })
    @PostMapping("/refresh")
    public ResponseEntity<Response<AuthLoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthLoginResponse response = authCommandService.refresh(request.getRefreshToken());

        return Response.success(response, AuthSuccessCode.TOKEN_REFRESH);
    }

    // 회원탈퇴
    @Operation(
            summary = "회원 삭제",
            description = "특정 회원을 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            })
    @DeleteMapping("/withdraw")
    public ResponseEntity<Response<Void>> withdraw(
            @Valid @RequestBody AuthWithdrawRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        authCommandService.withdraw(request, authUser);

        return Response.success(null, AuthSuccessCode.USER_WITHDRAW);
    }
}
