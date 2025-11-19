package org.example.ootoutfitoftoday.security.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
import org.example.ootoutfitoftoday.domain.user.service.command.UserCommandService;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.example.ootoutfitoftoday.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String REDIS_KEY_PREFIX = "oauth:temp:code:";

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${oauth.temp-code-ttl-minutes:3}")
    private long tempCodeTtlMinutes;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        try {
            log.info("=== OAuth2 인증 성공 시작 ===");

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                log.info("기존 세션 무효화: {}", oldSession.getId());
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            log.info("새 세션 생성: {}", session.getId());

            log.info("Request URI: {}", request.getRequestURI());
            log.info("Session ID: {}", session.getId());
            log.info("Session Creation Time: {}", new Date(session.getCreationTime()));

            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    log.info("Cookie - Name: {}, Value: {}, Path: {}, MaxAge: {}",
                            cookie.getName(),
                            cookie.getValue(),
                            cookie.getPath(),
                            cookie.getMaxAge());
                }
            } else {
                log.warn("쿠키가 없음");
            }

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String registrationId = extractRegistrationId(authentication);
            SocialProvider provider = getSocialProvider(registrationId);

            OAuth2UserInfo userInfo = OAuth2UserInfo.of(provider, oAuth2User.getAttributes());

            log.info("OAuth2 인증 성공 - provider: {}, email: {}, name: {}", provider, userInfo.getEmail(), userInfo.getName());

            User user = processUser(provider, userInfo);

            String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole());
            String refreshToken = jwtUtil.createRefreshToken(user.getId());

            log.info("JWT 토큰 생성 완료 - userId: {}", user.getId());

            String tempCode = generateAndSaveTempCode(user.getId(), accessToken, refreshToken);

            log.info("임시 코드 생성 완료 - code: {}, userId: {}", tempCode, user.getId());

            redirectToFrontend(response, tempCode);

        } catch (AuthException ae) {
            log.warn("OAuth2 인증 실패: {}", ae.getMessage());
            redirectToFrontendWithError(response, ae.getMessage());

        } catch (Exception e) {
            log.error("OAuth2 인증 처리 중 오류 발생", e);
            redirectToFrontendWithError(response, "OAuth2 인증 처리 중 오류가 발생했습니다.");
        }
    }

    private User processUser(SocialProvider provider, OAuth2UserInfo userInfo) {

        User user = null;

        Optional<User> userBySocialId = userQueryService.findBySocialProviderAndSocialId(provider, userInfo.getSocialId());

        if (userBySocialId.isPresent()) {
            user = userBySocialId.get();

            if (user.isDeleted()) {
                log.warn("소셜 로그인 유저가 삭제된 상태입니다: {}", userInfo.getEmail());
                throw new AuthException(AuthErrorCode.USER_ALREADY_WITHDRAWN);
            }

        } else {
            try {
                User userByEmail = userQueryService.findByEmailAndIsDeletedFalse(userInfo.getEmail());

                if (userByEmail.getSocialId() == null) {
                    log.info("일반 계정에 소셜 연동 진행: {}", userInfo.getEmail());
                    user = userCommandService.linkSocialAccount(
                            userByEmail,
                            provider,
                            userInfo.getSocialId(),
                            userInfo.getPicture()
                    );
                } else {
                    log.warn("계정 충돌 - 이미 다른 소셜 계정과 연동됨: {}", userInfo.getEmail());
                    throw new AuthException(AuthErrorCode.ACCOUNT_ALREADY_LINKED);
                }

            } catch (UserException e) {
                log.info("신규 회원가입 진행: {}", provider);
                user = createNewUser(provider, userInfo);
            }
        }

        if (user == null) {
            log.error("논리적 오류: 사용자 처리 실패 - email: {}", userInfo.getEmail());
            throw new AuthException(AuthErrorCode.ACCOUNT_ALREADY_LINKED);
        }

        return user;
    }

    private String generateAndSaveTempCode(
            Long userId,
            String accessToken,
            String refreshToken
    ) {
        try {
            String tempCode = UUID.randomUUID().toString();

            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("userId", userId.toString());
            tokenData.put("accessToken", accessToken);
            tokenData.put("refreshToken", refreshToken);

            String tokenJson;
            try {
                tokenJson = objectMapper.writeValueAsString(tokenData);
            } catch (JsonProcessingException e) {
                log.error("토큰 정보 JSON 직렬화 실패", e);
                throw new AuthException(AuthErrorCode.TOKEN_SERIALIZATION_FAILED);
            }

            String redisKey = REDIS_KEY_PREFIX + tempCode;
            try {
                redisTemplate.opsForValue().set(redisKey, tokenJson, tempCodeTtlMinutes, TimeUnit.MINUTES);
            } catch (RedisConnectionFailureException e) {
                log.error("Redis 연결 실패", e);
                throw new AuthException(AuthErrorCode.REDIS_CONNECTION_FAILED);
            }

            String storedValue = redisTemplate.opsForValue().get(redisKey);
            if (storedValue == null) {
                log.error("Redis 저장 검증 실패 - key: {}", redisKey);
                throw new AuthException(AuthErrorCode.REDIS_SAVE_FAILED);
            }

            log.info("Redis 저장 검증 성공 - key: {}, TTL: {}분", redisKey, tempCodeTtlMinutes);

            return tempCode;

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("임시 코드 생성 중 예상치 못한 오류", e);
            throw new AuthException(AuthErrorCode.OAUTH_LOGIN_FAILED);
        }
    }

    private void redirectToFrontend(HttpServletResponse response, String tempCode) throws IOException {

        String redirectUrl = String.format("%s/auth/callback?code=%s", frontendUrl, tempCode);

        log.info("=== 리다이렉트 직전 상태 ===");
        log.info("Temp Code: {}", tempCode);
        log.info("Redirect URL: {}", redirectUrl);
        log.info("Response committed: {}", response.isCommitted());

        log.debug("프론트엔드로 리다이렉트 - code: {}", tempCode);
        log.info("OAuth2 인증 완료 - 리다이렉트 수행");

        response.sendRedirect(redirectUrl);
    }

    private void redirectToFrontendWithError(HttpServletResponse response, String errorMessage)
            throws IOException {

        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        String redirectUrl = String.format("%s/login?error=%s", frontendUrl, encodedMessage);

        log.warn("에러로 인한 리다이렉트 - message: {}", errorMessage);
        response.sendRedirect(redirectUrl);
    }

    private String extractRegistrationId(Authentication authentication) {

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            return oauth2Token.getAuthorizedClientRegistrationId();
        }
        log.error("OAuth2AuthenticationToken이 아닌 Authentication 객체: {}", authentication.getClass().getName());
        throw new AuthException(AuthErrorCode.INVALID_OAUTH2_TOKEN);
    }

    private SocialProvider getSocialProvider(String registrationId) {

        return switch (registrationId.toLowerCase()) {
            case "google" -> SocialProvider.GOOGLE;
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자: " + registrationId);
        };
    }

    private User createNewUser(SocialProvider provider, OAuth2UserInfo userInfo) {
        String nickname = userCommandService.generateUniqueNickname(userInfo.getName());

        return userCommandService.createSocialUser(
                userInfo.getEmail(),
                nickname,
                userInfo.getName(),
                userInfo.getPicture(),
                provider,
                userInfo.getSocialId()
        );
    }
}