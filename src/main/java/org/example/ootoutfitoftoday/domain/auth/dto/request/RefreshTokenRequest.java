package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenRequest {

    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;

    // 디바이스 ID(토큰과 디바이스 일치 여부 검증용)
    @NotBlank(message = "디바이스 ID는 필수입니다.")
    private String deviceId;
}
