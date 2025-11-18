package org.example.ootoutfitoftoday.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.example.ootoutfitoftoday.security.jwt.JwtAuthenticationToken;
import org.example.ootoutfitoftoday.security.jwt.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        if (path.startsWith("/v1/internal/")) {
            log.debug("[JWT FILTER] Skipped for Internal API → {}", path);

            return true;
        }

        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars")) {

            return true;
        }

        if (path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/actuator/prometheus")) {

            return true;
        }

        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) {

            return true;
        }

        if (path.startsWith("/ws") || path.startsWith("/stomp")) {

            return true;
        }

        if ("POST".equalsIgnoreCase(method) &&
                (path.startsWith("/v1/auth/signup") ||
                        path.startsWith("/v1/auth/login") ||
                        path.startsWith("/v1/auth/refresh") ||
                        path.startsWith("/v1/auth/oauth2/token/exchange"))) {

            return true;
        }

        if ("GET".equalsIgnoreCase(method)) {
            if (path.startsWith("/v1/closets/public") ||
                    path.startsWith("/v1/sale-posts/public") ||
                    path.startsWith("/v1/categories") ||
                    path.startsWith("/v1/donation-centers/search")) {

                return true;
            }

            if (path.matches("/v1/closets/\\d+") ||
                    path.matches("/v1/sale-posts/\\d+")) {

                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {
        log.info("JwtAuthenticationFilter 진입: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

        String authorizationHeader = httpRequest.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            sendErrorResponse(httpResponse, HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다.");

            return;
        }

        String jwt = jwtUtil.substringToken(authorizationHeader);

        if (!processAuthentication(jwt, httpRequest, httpResponse)) {

            return;
        }

        chain.doFilter(httpRequest, httpResponse);
    }

    private boolean processAuthentication(
            String jwt,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            if (!jwtUtil.isAccessToken(jwt)) {
                log.warn("리프레시 토큰이 Authorization 헤더로 전송됨: URI={}", request.getRequestURI());
                sendErrorResponse(response, HttpStatus.BAD_REQUEST, "액세스 토큰이 필요합니다.");

                return false;
            }

            Claims claims = jwtUtil.extractClaims(jwt);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                setAuthentication(claims);
            }

            return true;

        } catch (SignatureException e) {
            log.warn("JWT 서명 불일치: URI={}", request.getRequestURI(), e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 서명입니다.");

            return false;

        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 형식: URI={}", request.getRequestURI(), e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "잘못된 JWT 토큰입니다.");

            return false;

        } catch (ExpiredJwtException e) {
            log.warn("JWT 만료: userId={}, URI={}", e.getClaims().getSubject(), request.getRequestURI());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다.");

            return false;

        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT: URI={}", request.getRequestURI(), e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다.");

            return false;

        } catch (Exception e) {
            log.error("예상치 못한 JWT 검증 오류: URI={}", request.getRequestURI(), e);
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

            return false;
        }
    }

    private void setAuthentication(Claims claims) {
        Long userId = Long.valueOf(claims.getSubject());
        UserRole userRole = UserRole.of(claims.get("userRole", String.class));

        AuthUser authUser = new AuthUser(userId, userRole);
        Authentication authenticationToken = new JwtAuthenticationToken(authUser);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void sendErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.name());
        errorResponse.put("code", status.value());
        errorResponse.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}