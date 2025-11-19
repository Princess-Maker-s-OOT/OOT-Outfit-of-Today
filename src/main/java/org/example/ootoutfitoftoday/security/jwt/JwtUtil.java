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

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_TIME = 15 * 60 * 1000L;
    private static final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000L;
    private static final String USER_ROLE_CLAIM = "userRole";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;
    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, UserRole userRole) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .id(UUID.randomUUID().toString())
                        .subject(String.valueOf(userId))
                        .claim(USER_ROLE_CLAIM, userRole.getUserRole())
                        .claim(TOKEN_TYPE_CLAIM, "access")
                        .expiration(new Date(date.getTime() + ACCESS_TOKEN_TIME))
                        .issuedAt(date)
                        .signWith(key, Jwts.SIG.HS256)
                        .compact();
    }

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

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {

            return tokenValue.substring(BEARER_PREFIX.length());
        }
        log.warn("잘못된 Authorization 헤더 형식이 감지되었습니다. tokenValue={}", tokenValue);
        throw new IllegalArgumentException("유효하지 않은 JWT 토큰 형식입니다.");
    }

    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isExpired(String token) {
        try {

            return extractClaims(token).getExpiration().before(new Date());

        } catch (ExpiredJwtException e) {

            return true;

        } catch (Exception e) {
            log.warn("Token validation failed for non-expiration reason: {}", e.getMessage());

            return true;
        }
    }

    public String getId(String token) {

        return extractClaims(token).getSubject();
    }

    public String getRole(String token) {

        return extractClaims(token).get(USER_ROLE_CLAIM, String.class);
    }

    public LocalDateTime calculateRefreshTokenExpiresAt() {

        return LocalDateTime.now().plusSeconds(REFRESH_TOKEN_TIME / 1000);
    }

    public String getTokenType(String token) {

        return extractClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    public boolean isAccessToken(String token) {

        return ACCESS_TOKEN_TYPE.equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {

        return REFRESH_TOKEN_TYPE.equals(getTokenType(token));
    }
}