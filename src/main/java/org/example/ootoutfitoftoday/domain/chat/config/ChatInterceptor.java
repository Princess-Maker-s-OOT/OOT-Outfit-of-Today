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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatInterceptor implements ChannelInterceptor {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_ = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (!StompCommand.CONNECT.equals(accessor.getCommand())) {

            return message;
        }

        String tokenWithPrefix = accessor.getFirstNativeHeader(AUTHORIZATION);

        if (!processAuthentication(accessor, tokenWithPrefix)) {

            return null;
        }

        return message;
    }

    private boolean processAuthentication(StompHeaderAccessor accessor, String tokenWithPrefix) {
        if (tokenWithPrefix == null || !tokenWithPrefix.startsWith(BEARER_)) {
            log.error("Authentication Failed: Missing or Invalid 'Authorization' header format.");
            throw new IllegalArgumentException("Invalid token format.");
        }

        try {
            String pureToken = jwtUtil.substringToken(tokenWithPrefix);

            Claims claims = jwtUtil.extractClaims(pureToken);

            String userIdString = claims.getSubject();
            String userRole = jwtUtil.getRole(pureToken);

            if (userIdString == null) {
                log.error("Authentication Failed: User ID (Subject) not found in token.");
                throw new IllegalArgumentException("Missing User ID in token.");
            }

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

    private Authentication createAuthentication(String userId, String userRole) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}