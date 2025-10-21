package org.example.ootoutfitoftoday.domain.chat.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.security.jwt.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * STOMP(WebSocket)의 채널 인터셉터 구현 클래스.
 * <p>
 * 이 인터셉터는 클라이언트가 웹소켓 연결(CONNECT)을 시도할 때,
 * 메시지 헤더의 'Authorization'에 포함된 JWT 토큰을 검증하고,
 * Spring Security의 Authentication 객체를 생성하여 웹소켓 세션에 설정(인증)하는 역할을 수행합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatInterceptor implements ChannelInterceptor {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_ = "Bearer ";

    // ⭐ 1. 필수 의존성 주입 (JwtUtil과 UserDetailsService)
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // CONNECT 명령이 아닐 경우, 인증 로직 없이 다음 단계로 전달
        if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        // 1. Authorization 헤더 추출
        String tokenWithPrefix = accessor.getFirstNativeHeader(AUTHORIZATION);

        // 2. 토큰 유효성 검증 및 인증 객체 설정
        if (!processAuthentication(accessor, tokenWithPrefix)) {
            // 인증 실패 시, 예외가 던져지거나 null이 반환되어 연결이 끊어집니다.
            return null;
        }

        return message;
    }

    /**
     * CONNECT 시 JWT 토큰을 검증하고 SecurityContext에 Authentication 객체를 설정합니다.
     *
     * @return 인증 성공 시 true, 실패 시 false (또는 예외 발생)
     */
    private boolean processAuthentication(StompHeaderAccessor accessor, String tokenWithPrefix) {

        // 2.1. 토큰 누락 및 형식 검사
        if (tokenWithPrefix == null || !tokenWithPrefix.startsWith(BEARER_)) {
            log.error("Authentication Failed: Missing or Invalid 'Authorization' header format.");
            // Spring이 ConnectionLostException을 발생시키도록 강제합니다.
            throw new IllegalArgumentException("Invalid token format.");
        }

        try {
            // Bearer 접두사 제거
            String pureToken = jwtUtil.substringToken(tokenWithPrefix);

            // JWT 유효성 검증 및 Claims 추출 (만료/서명 오류 시 여기서 예외 발생)
            Claims claims = jwtUtil.extractClaims(pureToken);

            String userIdString = claims.getSubject();
            String userRole = jwtUtil.getRole(pureToken);

            if (userIdString == null) {
                log.error("Authentication Failed: User ID (Subject) not found in token.");
                throw new IllegalArgumentException("Missing User ID in token.");
            }

            // 3. Authentication 객체 생성 및 세션에 설정
            Authentication authentication = createAuthentication(userIdString, userRole);
            accessor.setUser(authentication);

            log.info("STOMP Authentication Success: User ID {}", userIdString);
            return true;

        } catch (ExpiredJwtException e) {
            log.error("Authentication Failed: JWT Token Expired for Subject: {}", e.getClaims().getSubject());
            throw new IllegalArgumentException("Token expired.");

        } catch (SignatureException e) {
            log.error("Authentication Failed: Invalid JWT Signature. {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token signature.");

        } catch (UsernameNotFoundException e) {
            log.error("Authentication Failed: User ID not found in DB. ID: {}", e.getMessage());
            throw new IllegalArgumentException("User not found.");

        } catch (Exception e) {
            log.error("Authentication Failed: Unexpected error during token processing. {}", e.getMessage(), e);
            throw new IllegalArgumentException("Internal authentication error.");
        }
    }


    /**
     * JWT에서 추출한 ID와 역할을 바탕으로 Spring Security의 Authentication 객체를 생성합니다.
     */
    private Authentication createAuthentication(String userId, String userRole) {
        // userId는 String 타입으로 로드해야 합니다. Long.parseLong(userId)는 필요하지 않습니다.
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        // 2. Authentication 객체 생성
        return new UsernamePasswordAuthenticationToken(
                userDetails,      // Principal (사용자 정보: UserDetails 객체)
                null,             // Credentials (비밀번호: 웹소켓에서는 불필요하므로 null)
                userDetails.getAuthorities() // Authorities (사용자 권한: UserDetails에서 로드)
        );
    }
}