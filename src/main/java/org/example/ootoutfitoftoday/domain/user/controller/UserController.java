package org.example.ootoutfitoftoday.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
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

@Tag(name = "회원 관리", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user/me")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    // 회원정보 조회
    @Operation(
            summary = "내 정보 조회",
            description = "토큰을 기반으로 회원 자신의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
            })
    @GetMapping
    public ResponseEntity<Response<GetMyInfoResponse>> getMyInfo(@AuthenticationPrincipal AuthUser authUser) {

        GetMyInfoResponse response = userQueryService.getMyInfo(authUser.getUserId());

        return Response.success(response, UserSuccessCode.GET_MY_INFO);
    }

    // 회원정보 수정 전 비밀번호 검증
    @Operation(
            summary = "회원정보 수정 전 비밀번호 검증",
            description = "회원정보 수정 전 비밀번호를 검증합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            })
    @PostMapping("/password-verification")
    public ResponseEntity<Response<Void>> verifyPassword(
            @Valid @RequestBody UserPasswordVerificationRequest request,
            @AuthenticationPrincipal AuthUser authUser) {

        userQueryService.verifyPassword(request, authUser);

        return Response.success(null, UserSuccessCode.PASSWORD_VERIFIED);
    }

    // 회원정보 수정
    @Operation(
            summary = "회원 정보 수정",
            description = "기존 회원의 정보를 업데이트합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
            })
    @PatchMapping
    public ResponseEntity<Response<GetMyInfoResponse>> updateUserInfo(
            @Valid @RequestBody UserUpdateInfoRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        GetMyInfoResponse response = userCommandService.updateMyInfo(request, authUser);
        return Response.success(response, UserSuccessCode.UPDATE_MY_INFO);
    }
}