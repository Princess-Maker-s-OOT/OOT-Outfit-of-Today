package org.example.ootoutfitoftoday.domain.user.dto.request;

import lombok.Getter;

@Getter
public class UserPasswordVerificationRequest {

    /**
     * 비밀번호 필드
     *
     * @NotBlank(message = "비밀번호는 필수 입력값입니다.") 제거
     * - 일반 유저: 필수(@NotBlank 검증은 서비스에서 진행)
     * - 소셜 유저: 불필요(null 또는 빈 문자열 허용)
     * -> @NotBlank를 제거하여 소셜 회원의 경우 빈 값이나 null 허용
     */
    private String password;
}