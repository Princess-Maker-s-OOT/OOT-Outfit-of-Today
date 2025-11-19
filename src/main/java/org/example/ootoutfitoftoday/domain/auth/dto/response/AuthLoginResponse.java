package org.example.ootoutfitoftoday.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthLoginResponse {

    private final String accessToken;
    private final String refreshToken;
}
