package org.example.ootoutfitoftoday.security.oauth2;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;

import java.util.Map;

@Getter
@Builder
// 표준화된 소셜 사용자 정보 DTO
public class OAuth2UserInfo {

    private String socialId;    // 소셜 고유 ID(예: Google의 sub)
    private String email;
    private String name;
    private String picture;

    // 팩토리 메서드: 소셜 제공자에 따라 다른 attributes를 표준화
    public static OAuth2UserInfo of(SocialProvider provider, Map<String, Object> attributes) {

        return switch (provider) {
            // GOOGLE인 경우 구글 전용 변환 메서드 호출
            case GOOGLE -> ofGoogle(attributes);
        };
    }

    // 구글 Attributes를 표준 DTO로 변환
    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {

        return OAuth2UserInfo.builder()
                .socialId((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .picture((String) attributes.get("picture"))
                .build();
    }
}
