package org.example.ootoutfitoftoday.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.request.*;
import org.example.ootoutfitoftoday.domain.auth.dto.response.AuthLoginResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.response.DeviceInfoResponse;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthSuccessCode;
import org.example.ootoutfitoftoday.domain.auth.service.command.AuthCommandService;
import org.example.ootoutfitoftoday.domain.auth.service.query.AuthQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "회원 관리", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;

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
                    "- Refresh Token: 응답 바디에 포함 (7일)\n" +
                    "- Device ID: 클라이언트가 생성한 UUID 필수 전송",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 생성"),
                    @ApiResponse(responseCode = "401", description = "로그인 실패(잘못된 아이디 또는 비밀번호)")
            })
    @PostMapping("/login")
    public ResponseEntity<Response<AuthLoginResponse>> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletRequest httpRequest    // IP, User-Agent 추출용
    ) {
        AuthLoginResponse response = authCommandService.login(request, httpRequest);

        return Response.success(response, AuthSuccessCode.USER_LOGIN);
    }

    // 내 디바이스 목록 조회
    @Operation(
            summary = "내 디바이스 목록",
            description = "현재 로그인된 모든 디바이스 목록을 조회합니다.\n\n" +
                    "- 디바이스 ID, 이름, 마지막 사용 시간 등 포함\n" +
                    "- 최근 사용 순으로 정렬",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/devices")
    public ResponseEntity<Response<List<DeviceInfoResponse>>> getDevices(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String currentDeviceId
    ) {
        List<DeviceInfoResponse> devices = authQueryService.getDeviceList(authUser, currentDeviceId);

        return Response.success(devices, AuthSuccessCode.DEVICE_LIST_RETRIEVED);
    }

    // 토큰 재발급
    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.(RTR)\n\n" +
                    "- 리프레시 토큰은 Body로 전송\n" +
                    "- Device ID도 함께 전송하여 디바이스 검증",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
                    @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰")
            })
    @PostMapping("/refresh")
    public ResponseEntity<Response<AuthLoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        // deviceId 전달
        AuthLoginResponse response = authCommandService.refresh(request.getRefreshToken(), request.getDeviceId());

        return Response.success(response, AuthSuccessCode.TOKEN_REFRESH);
    }

    // OAuth2 임시 코드를 JWT 토큰으로 교환
    // OAuth2 로그인 후 프론트엔드가 받은 임시 코드를 실제 토큰으로 교환
    // 임시 코드는 3분간 유효하며 1회용
    @Operation(
            summary = "OAuth2 임시 코드 교환",
            description = "OAuth2 로그인 후 발급된 임시 코드를 JWT 토큰으로 교환합니다.\n\n" +
                    "- 임시 코드는 3분간 유효\n" +
                    "- 1회용(사용 후 자동 삭제)\n" +
                    "- Redis에서 토큰 정보 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 교환 성공"),
                    @ApiResponse(responseCode = "400", description = "유효하지 않거나 만료된 코드")
            })
    @PostMapping("/oauth2/token/exchange")
    public ResponseEntity<Response<AuthLoginResponse>> exchangeOAuthToken(
            @Valid @RequestBody TokenExchangeRequest request,
            HttpServletRequest httpRequest
    ) {

        AuthLoginResponse response = authCommandService.exchangeOAuthToken(request.getCode(), request.getDeviceId(), request.getDeviceName(), httpRequest);

        return Response.success(response, AuthSuccessCode.TOKEN_EXCHANGE);
    }

    // 로그아웃
    // deviceId 쿼리 파라미터 추가
    @Operation(
            summary = "로그아웃",
            description = "특정 디바이스에서 로그아웃하고 리프레시 토큰을 무효화합니다.\n\n" +
                    "- DB에서 해당 디바이스의 리프레시 토큰 삭제\n" +
                    "- 다른 디바이스는 계속 로그인 상태 유지",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String deviceId
    ) {
        authCommandService.logout(authUser, deviceId);

        return Response.success(null, AuthSuccessCode.USER_LOGOUT);
    }

    // 모든 디바이스에서 로그아웃
    @Operation(
            summary = "전체 로그아웃",
            description = "모든 디바이스에서 로그아웃합니다.\n\n" +
                    "- 모든 디바이스의 리프레시 토큰 삭제\n" +
                    "- 보안 위협 발생 시 사용",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "전체 로그아웃 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @PostMapping("/logout/all")
    public ResponseEntity<Response<Void>> logoutAll(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        authCommandService.logoutAll(authUser);

        return Response.success(null, AuthSuccessCode.USER_LOGOUT);
    }

    // 특정 디바이스 강제 로그아웃
    @Operation(
            summary = "디바이스 제거",
            description = "특정 디바이스를 강제로 로그아웃합니다.\n\n" +
                    "- 분실한 디바이스 또는 의심스러운 디바이스 제거\n" +
                    "- 해당 디바이스의 리프레시 토큰 삭제",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "디바이스 제거 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @DeleteMapping("/devices/{deviceId}")
    public ResponseEntity<Response<Void>> removeDevice(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String deviceId
    ) {
        authCommandService.removeDevice(authUser, deviceId);

        return Response.success(null, AuthSuccessCode.DEVICE_REMOVED);
    }

    // 회원탈퇴
    @Operation(
            summary = "회원 삭제",
            description = "특정 회원을 삭제합니다.\n\n" +
                    "- 일반 로그인 사용자: 비밀번호 검증 필수\n" +
                    "- 소셜 로그인 사용자: 비밀번호 검증 통과",
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