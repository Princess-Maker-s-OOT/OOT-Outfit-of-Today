package org.example.ootoutfitoftoday.domain.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginType {

    LOGIN_ID("아이디 로그인"),
    SOCIAL("소셜 로그인");

    private final String description;
}
