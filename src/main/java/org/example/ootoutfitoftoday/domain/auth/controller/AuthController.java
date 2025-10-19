package org.example.ootoutfitoftoday.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthSuccessCode;
import org.example.ootoutfitoftoday.domain.auth.service.command.AuthCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
