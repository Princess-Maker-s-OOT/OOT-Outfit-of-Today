package org.example.ootoutfitoftoday.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.entity.RefreshToken;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.auth.repository.RefreshTokenRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
import org.example.ootoutfitoftoday.domain.user.service.command.UserCommandService;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.example.ootoutfitoftoday.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
// 인증 성공 시 호출되는 핸들러, 리다이렉트 대신 JSON 응답을 위해 상속
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    // 리프레시 토큰 만료 시간 (기본값 7일) 주입
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiration;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication    // 인증 객체(OAuth2User 정보 포함)
    ) throws IOException {

        try {
            // OAuth2 인증 객체에서 사용자 정보(Principal) 추출
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // 요청 경로에서 소셜 제공자 식별(예: /login/oauth2/code/google -> google)
            String registrationId = extractRegistrationId(authentication);
            SocialProvider provider = getSocialProvider(registrationId);

            // 제공자별 사용자 정보를 표준화된 DTO로 변환
            OAuth2UserInfo userInfo = OAuth2UserInfo.of(provider, oAuth2User.getAttributes());

            log.info("OAuth2 인증 성공 - provider: {}, email: {}, name: {}",
                    provider, userInfo.getEmail(), userInfo.getName());

            // 계정 처리 로직 시작
            User user = null;             // null로 초기화
            boolean isNewUser = false;    // false로 초기화

            // 1. 소셜 ID로 기존 유저 확인(소셜 재로그인)
            Optional<User> userBySocialId = userQueryService.findBySocialProviderAndSocialId(provider, userInfo.getSocialId());

            if (userBySocialId.isPresent()) {
                // 2. 소셜 로그인 기록이 있는 기존 유저
                user = userBySocialId.get();

                // 3. 회원탈퇴한 유저인지 확인
                if (user.isDeleted()) {
                    log.warn("소셜 로그인 유저가 삭제된 상태입니다: {}", userInfo.getEmail());
                    throw new AuthException(AuthErrorCode.USER_ALREADY_WITHDRAWN);
                }

            } else {
                // 4. 소셜 ID로 찾지 못함. 이메일로 일반/미연동 계정 확인 시도
                try {
                    User userByEmail = userQueryService.findByEmailAndIsDeletedFalse(userInfo.getEmail());

                    if (userByEmail.getSocialId() == null) {
                        // 5. 이메일은 있지만 socialId가 null인 '일반 계정' 발견 -> 연동 처리
                        log.info("일반 계정에 소셜 연동 진행: {}", userInfo.getEmail());
                        // UserCommandService를 통해 연동 및 DB 저장
                        user = userCommandService.linkSocialAccount(
                                userByEmail,
                                provider,
                                userInfo.getSocialId(),
                                userInfo.getPicture()
                        );
                    }

                } catch (UserException e) {
                    // 6. 이메일로도 사용자를 찾지 못함 -> 완전히 신규 회원 가입
                    log.info("신규 회원가입 진행: {}", provider);
                    user = createNewUser(provider, userInfo);
                    isNewUser = true;
                }
            }

            // NullPointerException 방지 및 오류 처리 로직 추가
            if (user == null) {
                log.error("논리적 오류 발생: 1단계에서 잡히지 않은 유저가 4단계 연동에 실패했습니다. 이메일: {}", userInfo.getEmail());
                // NPE 방지 및 사용자에게 계정 충돌 오류 알림
                throw new AuthException(AuthErrorCode.ACCOUNT_ALREADY_LINKED);
            }

            // 액세스 & 리프레시 토큰 발급 + JSON 응답 반환
            issueTokensAndRespond(response, user, isNewUser);

        }

        // AuthException은 브라우저로 상세 메시지 전달
        catch (AuthException ae) {
            log.warn("OAuth2 인증 실패: {}", ae.getMessage());
            sendErrorResponse(response, ae.getMessage());

        } catch (Exception e) {
            log.error("OAuth2 인증 처리 중 오류 발생", e);
            sendErrorResponse(response, "OAuth2 인증 처리 중 오류가 발생했습니다.");
        }
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

    // 액세스 & 리프레시 토큰 발급 후 JSON으로 반환
    private void issueTokensAndRespond(
            HttpServletResponse response,
            User user,
            boolean isNewUser
    ) throws IOException {

        // 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole());
        // 리프레시 토큰 생성
        String refreshTokenValue = jwtUtil.createRefreshToken(user.getId());
        // 리프레시 토큰 DB에 저장 및 갱신
        saveRefreshToken(user, refreshTokenValue);

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("accessToken", accessToken);
        tokenResponse.put("refreshToken", refreshTokenValue);
        tokenResponse.put("isNewUser", isNewUser);    // 신규 유저 여부를 응답에 포함

        // 응답 설정(JSON 형태)
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // Map 객체를 JSON 형태로 직렬화하여 응답 본문에 작성
        new ObjectMapper().writeValue(response.getWriter(), tokenResponse);

        log.info("OAuth2 로그인 완료 - JSON으로 토큰 응답: userId={}, isNewUser={}", user.getId(), isNewUser);
    }

    // 레프레시 토큰 저장 또는 갱신
    private void saveRefreshToken(User user, String token) {

        // 만료 시간 계산
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        // 사용자 ID로 기존 리프레시 토큰 조회
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        // 기존 토큰이 있으면 갱신
                        existing -> existing.updateToken(token, expiresAt),
                        // 없으면 새로 생성하여 저장
                        () -> refreshTokenRepository.save(RefreshToken.create(user, token, expiresAt))
                );
    }

    // 에러 발생 시 JSON 형태로 반환
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        new ObjectMapper().writeValue(response.getWriter(), error);
    }
}