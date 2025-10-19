package org.example.ootoutfitoftoday.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class AuthLoginResponse {

    private final String token;

    public AuthLoginResponse(String token) {
        this.token = token;
    }
}
