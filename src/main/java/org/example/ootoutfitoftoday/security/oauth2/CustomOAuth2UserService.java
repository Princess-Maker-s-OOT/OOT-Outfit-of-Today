package org.example.ootoutfitoftoday.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

// OAuth2 사용자 정보를 로드 및 처리하는 서비스
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    // Spring Security가 인증 서버에서 액세스 토큰을 받은 후 자동으로 호출
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 부모 클래스의 메서드를 호출하여 OAuth2User 객체(사용자 정보 포함)를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("OAuth2 사용자 정보 로드: {}", oAuth2User.getAttributes());

        // 처리된 OAuth2User 객체를 반환(다음 단계인 SuccessHandler로 전달)
        return oAuth2User;
    }
}
