package org.example.ootoutfitoftoday.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserPasswordVerificationRequest {

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
}