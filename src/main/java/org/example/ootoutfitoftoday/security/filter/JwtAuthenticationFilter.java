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

    /**
     * JWT 인증이 필요 없는 경로를 정의
     * OncePerRequestFilter의 표준 메서드를 오버라이드하여
     * 필터 적용 여부를 한 곳에서 관리합니다.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // Internal API (배치 서버 전용)
        if (path.startsWith("/v1/internal/")) {
            log.debug("[JWT FILTER] Skipped for Internal API → {}", path);
            return true;
        }

        // Swagger & API Docs
        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars")) {
            return true;
        }

        // Actuator Health Check & Prometheus
        if (path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/actuator/prometheus")) {
            return true;
        }

        // OAuth2 소셜 로그인
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) {
            return true;
        }

        // WebSocket
        if (path.startsWith("/ws") || path.startsWith("/stomp")) {
            return true;
        }

        // POST 요청 - 인증 불필요 경로 (회원가입/로그인)
        if ("POST".equalsIgnoreCase(method) &&
                (path.startsWith("/v1/auth/signup") ||
                        path.startsWith("/v1/auth/login") ||
                        path.startsWith("/v1/auth/refresh") ||
                        path.startsWith("/v1/auth/oauth2/token/exchange"))) {
            return true;
        }

        // GET 요청 - 공개 API
        if ("GET".equalsIgnoreCase(method)) {
            // 공개 목록 조회
            if (path.startsWith("/v1/closets/public") ||
                    path.startsWith("/v1/sale-posts/public") ||
                    path.startsWith("/v1/categories") ||
                    path.startsWith("/v1/donation-centers/search")) {
                return true;
            }

            // 단건 조회 (패턴 매칭)
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

        // 필터 진입 테스트 로그
        log.info("JwtAuthenticationFilter 진입: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

        // JWT 인증 시작
        // HTTP 요청 헤더에서 "Authorization" 헤더값을 가져옴
        String authorizationHeader = httpRequest.getHeader("Authorization");

        // Authorization 헤더가 없거나 "Bearer "로 시작하지 않으면 JWT 인증을 건너뜀
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            sendErrorResponse(httpResponse, HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다.");

            return;
        }

        String jwt = jwtUtil.substringToken(authorizationHeader);

        // JWT 검증 및 인증 설정
        if (!processAuthentication(jwt, httpRequest, httpResponse)) {

            return;
        }

        // JWT 검증 성공 시 다음 필터로 요청 전달
        chain.doFilter(httpRequest, httpResponse);
    }

    // JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정하는 메서드
    private boolean processAuthentication(
            String jwt,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            // 액세스 토큰 타입 검증 추가
            if (!jwtUtil.isAccessToken(jwt)) {
                log.warn("리프레시 토큰이 Authorization 헤더로 전송됨: URI={}", request.getRequestURI());
                sendErrorResponse(response, HttpStatus.BAD_REQUEST, "액세스 토큰이 필요합니다.");

                return false;
            }

            // JWT 토큰을 파싱하여 Claims(토큰에 담긴 정보) 추출
            Claims claims = jwtUtil.extractClaims(jwt);

            // SecurityContext에 인증 정보가 없으면 설정(이미 인증된 경우 중복 설정 방지)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                setAuthentication(claims);
            }

            return true; // 검증 성공

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

    // JWT Claims에서 사용자 정보를 추출하여 Spring Security의 인증 정보 설정
    private void setAuthentication(Claims claims) {
        // JWT의 subject claim에서 사용자 ID 추출 (subject는 JWT 표준 claim)
        Long userId = Long.valueOf(claims.getSubject());
        // claim에서 사용자 권한 정보를 추출하여 enum으로 변환
        UserRole userRole = UserRole.of(claims.get("userRole", String.class));

        // 추출한 정보로 인증된 사용자 객체 생성
        AuthUser authUser = new AuthUser(userId, userRole);
        // Spring Security가 인식할 수 있는 Authentication 객체 생성
        Authentication authenticationToken = new JwtAuthenticationToken(authUser);
        // SecurityContext에 인증 정보 저장 - 이후 @AuthenticationPrincipal로 접근 가능
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