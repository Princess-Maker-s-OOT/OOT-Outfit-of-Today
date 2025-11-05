package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AuthLoginRequest {

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;

    // 클라이언트가 UUID 생성하여 전송
    @NotBlank(message = "디바이스 ID를 입력해주세요.")
    private String deviceId;

    // 디바이스 명
    private String deviceName;
}
