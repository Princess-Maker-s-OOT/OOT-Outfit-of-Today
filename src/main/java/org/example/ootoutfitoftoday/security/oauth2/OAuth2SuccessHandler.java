package org.example.ootoutfitoftoday.security.oauth2;

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
// 인증 성공 시 호출되는 핸들러, 리다이렉트 대신 JSON 응답을 위해 상속
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // 래디스 키 접두사
    private static final String REDIS_KEY_PREFIX = "oauth:temp:code:";

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;  // Redis 추가
    private final ObjectMapper objectMapper;          // JSON 직렬화용

    // 임시 코드 TTL(3분)
    @Value("${oauth.temp-code-ttl-minutes:3}")
    private long tempCodeTtlMinutes;

    // 프론트엔드 URL 설정
    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication    // 인증 객체(OAuth2User 정보 포함)
    ) throws IOException {

        try {
            log.info("=== OAuth2 인증 성공 시작 ===");

            // 기존 세션 완전히 무효화하고 새 세션 생성
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                log.info("기존 세션 무효화: {}", oldSession.getId());
                oldSession.invalidate();
            }

            // 새 세션 생성
            HttpSession session = request.getSession(true);
            log.info("새 세션 생성: {}", session.getId());

            log.info("Request URI: {}", request.getRequestURI());
            log.info("Session ID: {}", session.getId());
            log.info("Session Creation Time: {}", new Date(session.getCreationTime()));

            // 쿠키 상세 로깅
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

            // OAuth2 인증 객체에서 사용자 정보(Principal) 추출
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // 요청 경로에서 소셜 제공자 식별(예: /login/oauth2/code/google -> google)
            String registrationId = extractRegistrationId(authentication);
            SocialProvider provider = getSocialProvider(registrationId);

            // 제공자별 사용자 정보를 표준화된 DTO로 변환
            OAuth2UserInfo userInfo = OAuth2UserInfo.of(provider, oAuth2User.getAttributes());

            log.info("OAuth2 인증 성공 - provider: {}, email: {}, name: {}", provider, userInfo.getEmail(), userInfo.getName());

            // 사용자 처리(기존 유저 확인 또는 신규 생성)
            User user = processUser(provider, userInfo);

            // JWT 생성
            String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole());
            String refreshToken = jwtUtil.createRefreshToken(user.getId());

            log.info("JWT 토큰 생성 완료 - userId: {}", user.getId());


            // 임시 코드 생성 및 래디스 저장
            String tempCode = generateAndSaveTempCode(user.getId(), accessToken, refreshToken);

            log.info("임시 코드 생성 완료 - code: {}, userId: {}", tempCode, user.getId());

            // 프론트엔드로 리다이렉트
            redirectToFrontend(response, tempCode);

        } catch (AuthException ae) {
            log.warn("OAuth2 인증 실패: {}", ae.getMessage());
            redirectToFrontendWithError(response, ae.getMessage());

        } catch (Exception e) {
            log.error("OAuth2 인증 처리 중 오류 발생", e);
            redirectToFrontendWithError(response, "OAuth2 인증 처리 중 오류가 발생했습니다.");
        }
    }

    // 사용자 처리 로직
    private User processUser(SocialProvider provider, OAuth2UserInfo userInfo) {

        User user = null;

        // 소셜 ID로 기존 유저 확인(소셜 재로그인)
        Optional<User> userBySocialId = userQueryService.findBySocialProviderAndSocialId(provider, userInfo.getSocialId());

        if (userBySocialId.isPresent()) {
            // 소셜 로그인 기록이 있는 기존 유저
            user = userBySocialId.get();

            // 회원탈퇴한 유저인지 확인
            if (user.isDeleted()) {
                log.warn("소셜 로그인 유저가 삭제된 상태입니다: {}", userInfo.getEmail());
                throw new AuthException(AuthErrorCode.USER_ALREADY_WITHDRAWN);
            }

        } else {
            // 소셜 ID로 찾지 못한 경우, 이메일로 일반/미연동 계정 확인 시도
            try {
                User userByEmail = userQueryService.findByEmailAndIsDeletedFalse(userInfo.getEmail());

                if (userByEmail.getSocialId() == null) {
                    // 이메일은 있지만 socialId가 null인 '일반 계정' 발견 -> 연동 처리
                    log.info("일반 계정에 소셜 연동 진행: {}", userInfo.getEmail());
                    // UserCommandService를 통해 연동 및 DB 저장
                    user = userCommandService.linkSocialAccount(
                            userByEmail,
                            provider,
                            userInfo.getSocialId(),
                            userInfo.getPicture()
                    );
                } else {
                    // 명시적으로 계정 충돌 상황 로깅
                    // 현재는 구글 로그인만 있어 무의미하긴 함. 추후 확장 고려
                    log.warn("계정 충돌 - 이미 다른 소셜 계정과 연동됨: {}", userInfo.getEmail());
                    throw new AuthException(AuthErrorCode.ACCOUNT_ALREADY_LINKED);
                }

            } catch (UserException e) {
                //  이메일로도 사용자를 찾지 못함 -> 완전히 신규 회원 가입
                log.info("신규 회원가입 진행: {}", provider);
                user = createNewUser(provider, userInfo);
            }
        }

        // NullPointerException 방지 및 오류 처리 로직 추가
        if (user == null) {
            log.error("논리적 오류: 사용자 처리 실패 - email: {}", userInfo.getEmail());
            // NPE 방지 및 사용자에게 계정 충돌 오류 알림
            throw new AuthException(AuthErrorCode.ACCOUNT_ALREADY_LINKED);
        }

        return user;
    }

    // 임시 코드 생성 및 Redis 저장
    private String generateAndSaveTempCode(
            Long userId,
            String accessToken,
            String refreshToken
    ) {
        try {
            // UUID로 임시 코드 생성
            String tempCode = UUID.randomUUID().toString();

            // 토큰 정보를 Map에 담기
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("userId", userId.toString());
            tokenData.put("accessToken", accessToken);
            tokenData.put("refreshToken", refreshToken);

            // Map을 JSON 문자열로 직렬화
            String tokenJson = objectMapper.writeValueAsString(tokenData);

            // 래디스에 저장(키: oauth:temp:code:{UUID}, TTL: 3분)
            String redisKey = REDIS_KEY_PREFIX + tempCode;
            redisTemplate.opsForValue().set(redisKey, tokenJson, tempCodeTtlMinutes, TimeUnit.MINUTES);

            // 래디스 저장 즉시 검증
            String storedValue = redisTemplate.opsForValue().get(redisKey);
            if (storedValue != null) {
                log.info("Redis 저장 검증 성공 - key: {}, TTL: {}분", redisKey, tempCodeTtlMinutes);
            } else {
                log.error("Redis 저장 검증 실패 - key: {}", redisKey);
                throw new RuntimeException("Redis 저장 실패");
            }

            return tempCode;

        } catch (Exception e) {
            log.error("임시 코드 생성 실패", e);
            throw new AuthException(AuthErrorCode.OAUTH_LOGIN_FAILED);
        }
    }

    // 프론트엔드로 리다이렉트(임시 코드 포함)
    private void redirectToFrontend(HttpServletResponse response, String tempCode) throws IOException {

        // 프론트엔드 콜백 URL 생성
        String redirectUrl = String.format("%s/auth/callback?code=%s", frontendUrl, tempCode);

        log.info("=== 리다이렉트 직전 상태 ===");
        log.info("Temp Code: {}", tempCode);
        log.info("Redirect URL: {}", redirectUrl);
        log.info("Response committed: {}", response.isCommitted());

        // 민감정보는 debug 레벨로
        log.debug("프론트엔드로 리다이렉트 - code: {}", tempCode);
        log.info("OAuth2 인증 완료 - 리다이렉트 수행");

        // 리다이렉트 실행(HTTP 302)
        response.sendRedirect(redirectUrl);
    }

    // 에러 발생 시 프론트엔드로 리다이렉트
    private void redirectToFrontendWithError(HttpServletResponse response, String errorMessage)
            throws IOException {

        //  URL 인코딩 추가 -> errorMessage에 한글 또는 공백 포함 시 URL 깨짐 방지
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        String redirectUrl = String.format("%s/login?error=%s", frontendUrl, encodedMessage);

        log.warn("에러로 인한 리다이렉트 - message: {}", errorMessage);
        response.sendRedirect(redirectUrl);
    }

    // 요청 경로에서 소셜 제공자 ID 추출(예: /.../google -> google)
    // Spring Security가 이미 저장해둔 Authentication 객체에서 추출
    private String extractRegistrationId(Authentication authentication) {

        // authentication이 OAuth2AuthenticationToken인지 확인 및 다운캐스팅
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            return oauth2Token.getAuthorizedClientRegistrationId();
        }
        // 이미 파싱된 registrationId를 가져오기만 함
        log.error("OAuth2AuthenticationToken이 아닌 Authentication 객체: {}", authentication.getClass().getName());
        throw new AuthException(AuthErrorCode.INVALID_OAUTH2_TOKEN);
    }

    // 추출된 ID를 SocialProvider Enum으로 변환
    private SocialProvider getSocialProvider(String registrationId) {

        return switch (registrationId.toLowerCase()) {
            case "google" -> SocialProvider.GOOGLE;
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자: " + registrationId);
        };
    }

    // 신규 사용자 생성(UserCommandService 호출)
    private User createNewUser(SocialProvider provider, OAuth2UserInfo userInfo) {

        // 이름 기반으로 고유한 닉네임 생성
        String nickname = userCommandService.generateUniqueNickname(userInfo.getName());

        // 소셜 사용자 생성 서비스 호출
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