package org.example.ootoutfitoftoday.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateInfoRequest;
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

    // 회원정보 조회
    @GetMapping
    public ResponseEntity<ApiResponse<GetMyInfoResponse>> getMyInfo(@AuthenticationPrincipal AuthUser authUser) {

        GetMyInfoResponse response = userQueryService.getMyInfo(authUser.getUserId());

        return ApiResponse.success(response, UserSuccessCode.GET_MY_INFO);
    }

    // 회원정보 수정 전 비밀번호 검증
    @PostMapping("/password-verification")
    public ResponseEntity<ApiResponse<Void>> verifyPassword(
            @RequestBody UserPasswordVerificationRequest request,
            @AuthenticationPrincipal AuthUser authUser) {

        userQueryService.verifyPassword(request, authUser);

        return ApiResponse.success(null, UserSuccessCode.PASSWORD_VERIFIED);
    }

    // 회원정보 수정
    @PatchMapping
    public ResponseEntity<ApiResponse<GetMyInfoResponse>> updateUserInfo(
            @RequestBody UserUpdateInfoRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        GetMyInfoResponse response = userCommandService.updateMyInfo(request, authUser);
        return ApiResponse.success(response, UserSuccessCode.UPDATE_MY_INFO);
    }
}