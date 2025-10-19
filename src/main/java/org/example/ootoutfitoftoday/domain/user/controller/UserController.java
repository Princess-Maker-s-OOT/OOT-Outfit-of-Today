package org.example.ootoutfitoftoday.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserGetResponse;
import org.example.ootoutfitoftoday.domain.user.exception.UserSuccessCode;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
public class UserController {

    private final UserQueryServiceImpl userQueryService;

    // 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserGetResponse>> getMyProfile(@AuthenticationPrincipal AuthUser authUser) {

        UserGetResponse response = userQueryService.getMyProfile(authUser.getUserId());

        return ApiResponse.success(response, UserSuccessCode.GET_MY_PROFILE);
    }
}
