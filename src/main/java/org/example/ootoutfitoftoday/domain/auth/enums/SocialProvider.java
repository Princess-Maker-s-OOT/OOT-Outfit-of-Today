package org.example.ootoutfitoftoday.domain.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {

    GOOGLE("구글");

    private final String description;
}
