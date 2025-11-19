package org.example.ootoutfitoftoday.security.oauth2;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;

import java.util.Map;

@Getter
@Builder
public class OAuth2UserInfo {

    private String socialId;
    private String email;
    private String name;
    private String picture;

    public static OAuth2UserInfo of(SocialProvider provider, Map<String, Object> attributes) {

        return switch (provider) {
            case GOOGLE -> ofGoogle(attributes);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {

        return OAuth2UserInfo.builder()
                .socialId((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .picture((String) attributes.get("picture"))
                .build();
    }
}