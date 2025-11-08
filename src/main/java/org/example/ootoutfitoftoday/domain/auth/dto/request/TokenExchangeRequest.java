package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * OAuth2 임시 코드 교환 요청 DTO
 * - 프론트엔드가 OAuth2 콜백에서 받은 임시 코드를 JWT로 교환
 */
@Getter
public class TokenExchangeRequest {

    @NotBlank(message = "임시 코드는 필수입니다")
    private String code;
}
