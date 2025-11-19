package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenExchangeRequest {

    @NotBlank(message = "임시 코드는 필수입니다")
    private String code;

    @NotBlank(message = "디바이스 ID는 필수입니다.")
    private String deviceId;

    @NotBlank(message = "디바이스 이름은 필수입니다.")
    private String deviceName;
}
