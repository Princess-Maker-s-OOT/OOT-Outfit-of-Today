package org.example.ootoutfitoftoday.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateInfoRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateTradeLocationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.GetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.exception.UserSuccessCode;
import org.example.ootoutfitoftoday.domain.user.service.command.UserCommandService;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user/me")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    /**
     * 회원정보 조회
     *
     * @param authUser 토큰 정보
     * @return 회원정보 반환
     */
    @GetMapping
    public ResponseEntity<ApiResponse<GetMyInfoResponse>> getMyInfo(
            @AuthenticationPrincipal AuthUser authUser
    ) {

        GetMyInfoResponse response = userQueryService.getMyInfo(authUser.getUserId());

        return ApiResponse.success(response, UserSuccessCode.GET_MY_INFO);
    }

    /**
     * 회원정보 수정 전 비밀번호 검증
     *
     * @param request  유저 비밀번호
     * @param authUser 토큰 정보
     * @return 공통 응답
     */
    @PostMapping("/password-verification")
    public ResponseEntity<ApiResponse<Void>> verifyPassword(
            @Valid @RequestBody UserPasswordVerificationRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        userQueryService.verifyPassword(request, authUser);

        return ApiResponse.success(null, UserSuccessCode.PASSWORD_VERIFIED);
    }

    /**
     * 회원정보 수정
     *
     * @param request  유저 수정 정보
     * @param authUser 토큰 정보
     * @return 업데이트 된 유저 정보
     */
    @PatchMapping
    public ResponseEntity<ApiResponse<GetMyInfoResponse>> updateUserInfo(
            @Valid @RequestBody UserUpdateInfoRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        GetMyInfoResponse response = userCommandService.updateMyInfo(request, authUser);

        return ApiResponse.success(response, UserSuccessCode.UPDATE_MY_INFO);
    }

    /**
     * 회원 거래 위치 수정
     *
     * @param request  거래 위치 주소, 좌표
     * @param authUser 토큰 정보
     * @return 공통 응답
     */
    @PatchMapping("/locations")
    public ResponseEntity<ApiResponse<Void>> updateUserTradeLocation(
            @RequestBody UserUpdateTradeLocationRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        userCommandService.updateMyTradeLocation(request, authUser.getUserId());

        return ApiResponse.success(null, UserSuccessCode.UPDATED_TRADE_LOCATION);
    }
}