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
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final long TOKEN_TIME = 60 * 60 * 1000L; // 60분
    private static final String USER_ROLE_CLAIM = "userRole";

    @Value("${jwt.secret.key}")
    private String secretKey;
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(
            Long userId,
            UserRole userRole
    ) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .id(UUID.randomUUID().toString())              // jti: 토큰 고유 식별자 (블랙리스트용)
                        .subject(String.valueOf(userId))
                        .claim(USER_ROLE_CLAIM, userRole.getUserRole())            // 인가용
                        .expiration(new Date(date.getTime() + TOKEN_TIME))
                        .issuedAt(date)                                       // 발급일
                        .signWith(key, Jwts.SIG.HS256)                        // 암호화 알고리즘
                        .compact();
    }

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {

            return tokenValue.substring(BEARER_PREFIX.length());
        }
        log.error("Not Found Token");
        throw new NullPointerException("Not Found Token");
    }

    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ----------------------------------------------------
    // ⭐ [추가됨] 토큰의 만료 여부를 확인합니다.
    // ----------------------------------------------------
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
    
    public String getId(String token) {
        // Claims 추출 시 이미 유효성 검사가 완료되었어야 합니다.
        return extractClaims(token).getSubject();
    }

    public String getRole(String token) {
        // USER_ROLE_CLAIM 키와 String 타입을 명시하여 커스텀 클레임을 추출합니다.
        return extractClaims(token).get(USER_ROLE_CLAIM, String.class);
    }
}
