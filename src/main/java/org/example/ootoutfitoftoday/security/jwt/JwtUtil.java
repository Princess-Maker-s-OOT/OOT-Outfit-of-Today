package org.example.ootoutfitoftoday.security.jwt;

import io.jsonwebtoken.Claims;
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
                        .id(UUID.randomUUID().toString())                     // JWT 표준 jti claim -> 블랙리스트 관리용
                        .subject(UUID.randomUUID().toString())                // 토큰 고유 식별자
                        .claim("userId", userId)                              // 사용자 식별용
                        .claim("userRole", userRole.getUserRole())            // 인가용
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
}
