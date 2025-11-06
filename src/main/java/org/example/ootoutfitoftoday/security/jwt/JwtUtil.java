package org.example.ootoutfitoftoday.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 * - Access Token(API 인증용): 짧은 만료 시간(60분), 헤더 전달
 * - Refresh Token(토큰 재발급용): 긴 만료 시간(7일), 쿠키 전달
 */
@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_TIME = 60 * 60 * 1000L;
    private static final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000L;
    private static final String USER_ROLE_CLAIM = "userRole";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.secret.key}")
    private String secretKey;
    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 액세스 토큰 생성
     * - 사용자 인증 시 발급
     * - API 요청 시 Authorization 헤더에 포함되어 전달됨
     * - Bearer 접두사 포함
     * - 유효기간 짧음(1시간)
     * -> 리프레시 토큰과의 구분을 위해 메서드 명 변경(createToken -> createAccessToken)
     */
    public String createAccessToken(Long userId, UserRole userRole) {

        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .id(UUID.randomUUID().toString())                            // jti: 토큰 고유 식별자 (블랙리스트용)
                        .subject(String.valueOf(userId))
                        .claim(USER_ROLE_CLAIM, userRole.getUserRole())              // 인가용
                        .claim(TOKEN_TYPE_CLAIM, "access")                           // 토큰 타입 구분
                        .expiration(new Date(date.getTime() + ACCESS_TOKEN_TIME))
                        .issuedAt(date)                                              // 발급일
                        .signWith(key, Jwts.SIG.HS256)                               // 암호화 알고리즘
                        .compact();
    }

    /**
     * 리프레시 토큰 생성
     * - 액세스 토큰 만료 시 재발급 용도로 사용
     * - 쿠키에 저장
     * - Bearer 접두사 없음
     * - 유효기간 김(7일)
     */
    public String createRefreshToken(Long userId) {

        Date date = new Date();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim(TOKEN_TYPE_CLAIM, "refresh")
                .expiration(new Date(date.getTime() + REFRESH_TOKEN_TIME))
                .issuedAt(date)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * "Bearer " 접두사 제거(액세스 토큰만 해당)
     * - Authorization 헤더 값에서 순수 JWT만 추출
     */
    public String substringToken(String tokenValue) {

        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {

            return tokenValue.substring(BEARER_PREFIX.length());
        }
        log.warn("잘못된 Authorization 헤더 형식이 감지되었습니다. tokenValue={}", tokenValue);
        throw new IllegalArgumentException("유효하지 않은 JWT 토큰 형식입니다.");
    }

    /**
     * JWT Claims 추출, 토큰 파싱(액세스, 리프레시 공통)
     * - 만료된 토큰이어도 Claims 내용은 추출 가능
     */
    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    /**
     * 토큰 만료 여부 확인(액세스, 리프레시 공통)
     */
    public boolean isExpired(String token) {

        try {

            // Claims 추출에 성공하면 (만료되지 않았음을 의미) false 반환
            return extractClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {

            // 만료 예외가 발생하면 true 반환
            return true;
        } catch (Exception e) {
            // 그 외 다른 예외 발생 시 (예: 서명 오류) 유효하지 않은 것으로 간주하고 처리
            log.warn("Token validation failed for non-expiration reason: {}", e.getMessage());

            return true;
        }
    }

    /**
     * 추후 외부 시스템(예: OAuth, 소셜 로그인, 마이크로서비스 간 인증 등)과 연동할 예정이므로,
     * String 타입으로 관리
     * 토큰에서 userId 추출(액세스, 리프레시 공통)
     */
    public String getId(String token) {

        // Claims 추출 시 이미 유효성 검사가 완료되었어야 합니다.
        return extractClaims(token).getSubject();
    }

    /**
     * 액세스 토큰에서 사용자 역할 추출(액세스 전용)
     * - 액세스 토큰에만 userRole 클레임이 존재함
     */
    public String getRole(String token) {

        // USER_ROLE_CLAIM 키와 String 타입을 명시하여 커스텀 클레임을 추출합니다.
        return extractClaims(token).get(USER_ROLE_CLAIM, String.class);
    }

    /**
     * 리프레시 토큰 만료 시간 조회용(밀리초)
     * - 쿠키로 클라이언트에 전달될 때만 필요
     * -> 쿠키의 maxAge를 JWT 만료 시간과 맞추기 위해 사용
     * - 서버(DB)에 저장된 토큰 유효성 확인용으로는 불필요
     */
    public long getRefreshTokenExpirationMillis() {

        return REFRESH_TOKEN_TIME;
    }

    /**
     * 리프레시 토큰 만료 시간 계산
     *
     * @return 현재 시간 + 리프레시 토큰 유효 기간
     */
    public LocalDateTime calculateRefreshTokenExpiresAt() {

        return LocalDateTime.now().plusSeconds(REFRESH_TOKEN_TIME / 1000);
    }

    /**
     * 토큰 타입(액세스 or 리프레시) 확인 및 추출(액세스, 리프레시 공통)
     * - isAccessToken(), isRefreshToken()에서 내부적으로 사용
     */
    public String getTokenType(String token) {

        return extractClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    /**
     * 액세스 토큰 판별
     * - JwtAuthenticationFilter에서 Authorization 헤더 검증 시 사용
     * - refresh() 엔드포인트에 액세스 토큰이 전송되는 것을 방지
     */
    public boolean isAccessToken(String token) {

        return ACCESS_TOKEN_TYPE.equals(getTokenType(token));
    }

    /**
     * 리프레시 토큰 판별
     * - refresh() 메서드에서 리프레시 토큰 타입 검증 시 사용
     * - 액세스 토큰이 잘못 전송되는 것을 방지
     */
    public boolean isRefreshToken(String token) {

        return REFRESH_TOKEN_TYPE.equals(getTokenType(token));
    }
}