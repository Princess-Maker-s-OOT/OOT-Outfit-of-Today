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
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateProfileImageRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateTradeLocationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserGetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserUpdateInfoResponse;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserUpdateProfileImageResponse;
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

    /**
     * 회원정보 조회
     *
     * @param authUser 토큰 정보
     * @return 회원정보 반환
     */
    @Operation(
            summary = "내 정보 조회",
            description = "토큰을 기반으로 회원 자신의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
            })
    @GetMapping
    public ResponseEntity<Response<UserGetMyInfoResponse>> getMyInfo(@AuthenticationPrincipal AuthUser authUser) {

        UserGetMyInfoResponse response = userQueryService.getMyInfo(authUser.getUserId());

        return Response.success(response, UserSuccessCode.GET_MY_INFO);
    }

    /**
     * 회원정보 수정 전 비밀번호 검증
     *
     * @param request  유저 비밀번호
     * @param authUser 토큰 정보
     * @return 공통 응답
     */
    @Operation(
            summary = "회원정보 수정 전 비밀번호 검증",
            description = "회원정보 수정 전 비밀번호를 검증합니다.\n\n" +
                    "- 일반 로그인 사용자: 비밀번호 검증 필수\n" +
                    "- 소셜 로그인 사용자: 비밀번호 검증 통과",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            })
    @PostMapping("/password-verification")
    public ResponseEntity<Response<Void>> verifyPassword(
            @Valid @RequestBody UserPasswordVerificationRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        userQueryService.verifyPassword(request, authUser);

        return Response.success(null, UserSuccessCode.PASSWORD_VERIFIED);
    }

    /**
     * 회원정보 수정
     *
     * @param request  유저 수정 정보
     * @param authUser 토큰 정보
     * @return 업데이트 된 유저 정보
     */
    @Operation(
            summary = "회원 정보 수정",
            description = "기존 회원의 정보를 업데이트합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
            })
    @PatchMapping
    public ResponseEntity<Response<UserUpdateInfoResponse>> updateInfo(
            @Valid @RequestBody UserUpdateInfoRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        UserUpdateInfoResponse response = userCommandService.updateInfo(request, authUser);

        return Response.success(response, UserSuccessCode.UPDATE_INFO);
    }

    @Operation(
            summary = "프로필 이미지 수정",
            description = "회원의 프로필 이미지를 업데이트합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음"),
            })
    @PutMapping("/profile-image")
    public ResponseEntity<Response<UserUpdateProfileImageResponse>> updateProfileImage(
            @Valid @RequestBody UserUpdateProfileImageRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UserUpdateProfileImageResponse response = userCommandService.updateProfileImage(authUser.getUserId(), request.getImageId()
        );

        return Response.success(response, UserSuccessCode.UPDATE_PROFILE_IMAGE);
    }

    @Operation(
            summary = "프로필 이미지 삭제",
            description = "회원의 프로필 이미지를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음"),
            })
    @DeleteMapping("/profile-image")
    public ResponseEntity<Response<Void>> deleteProfileImage(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userCommandService.deleteProfileImage(authUser.getUserId());

        return Response.success(null, UserSuccessCode.DELETE_PROFILE_IMAGE);
    }

    /**
     * 회원 거래 위치 수정
     *
     * @param request  거래 위치 주소, 좌표
     * @param authUser 토큰 정보
     * @return 공통 응답
     */
    @PatchMapping("/locations")
    public ResponseEntity<Response<Void>> updateUserTradeLocation(
            @RequestBody UserUpdateTradeLocationRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        userCommandService.updateMyTradeLocation(request, authUser.getUserId());

        return Response.success(null, UserSuccessCode.UPDATED_TRADE_LOCATION);
    }
}