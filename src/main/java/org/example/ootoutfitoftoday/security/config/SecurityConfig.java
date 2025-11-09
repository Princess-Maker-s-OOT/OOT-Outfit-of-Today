package org.example.ootoutfitoftoday.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.example.ootoutfitoftoday.security.filter.JwtAuthenticationFilter;
import org.example.ootoutfitoftoday.security.oauth2.CustomOAuth2UserService;
import org.example.ootoutfitoftoday.security.oauth2.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Configuration
//@RequiredArgsConstructor
@EnableWebSecurity                              // Spring Security 활성화
@EnableMethodSecurity(securedEnabled = true)    // @Secured 활성화
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ObjectMapper objectMapper;

    // TODO: CORS 설정(추후 수정 예정)
    @Value("${spring.cors.allowed-origins}")
    private String allowedOrigins;

    // 프론트엔드 URL 설정
    @Value("${frontend.url}")
    private String frontendUrl;

    // 순환참조 문제 발생 -> 해결을 위해 @Lazy(수동 생성자 필요) 사용
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Lazy OAuth2SuccessHandler oAuth2SuccessHandler,
            CustomOAuth2UserService customOAuth2UserService,
            ObjectMapper objectMapper
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));    // 프론트엔드 도메인
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 와일드카드 대신 필요한 헤더만 명시
        configuration.setAllowedHeaders(List.of(
                "Authorization",      // JWT 토큰
                "Content-Type",       // 요청 본문 타입
                "Accept",             // 응답 타입
                "X-Requested-With",   // AJAX 식별
                "Cookie",
                "Set-Cookie"
        ));
        // 응답 헤더 노출
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .cors(Customizer.withDefaults())    // CORS 규칙 활성화를 위해 위에서 정의한 빈을 Spring Security 내에 적용하는 코드
                .csrf(AbstractHttpConfigurer::disable)
                /**
                 * OAuth2는 다단계 인증 플로우(authorization code → token exchange) 거치고 있음
                 * 위 과정에서 Spring Security가 중간 상태를 세션에 저장해야 함
                 * 이전의 STATELESS로 설정으로 세션을 아예 사용하지 않아서 상태 소실 문제 발생
                 * 따라서 아래와 같이 고침
                 */
                // OAuth2 경로는 세션 기반으로 동작해야 하므로 허용, 나머지 JWT API는는 STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)    // OAuth2 경로에서만 세션 생성
                        .sessionFixation().migrateSession()                          // 세션 고정 공격 방지
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)                             // 새 세션 생성 즉 동시 로그인 허용
                )
                .addFilterBefore(jwtAuthenticationFilter, SecurityContextHolderAwareRequestFilter.class)    // JwtAuthenticationFilter를 스프링 시큐리티 인증 프로세스 전에 진행

                // JWT 사용 시 불필요한 기능들 비활성화
                .formLogin(AbstractHttpConfigurer::disable)      // [SSR] 서버가 로그인 HTML 폼 렌더링
                //.anonymous(AbstractHttpConfigurer::disable)      // 역명 사용자 허용
                .httpBasic(AbstractHttpConfigurer::disable)      // [SSR] 인증 팝업
                .logout(AbstractHttpConfigurer::disable)         // [SSR] 서버가 세션 무효화 후 리다이렉트
                .rememberMe(AbstractHttpConfigurer::disable)     // 서버가 쿠키 발급하여 자동 로그인

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // OAuth2 경로는 에러 핸들러 적용 제외
                            if (request.getRequestURI().startsWith("/oauth2") || request.getRequestURI().startsWith("/login/oauth2")) {
                                response.sendRedirect(frontendUrl + "/login?error=auth_failed");

                                return;
                            }
                            writeErrorResponse(response, request, HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeErrorResponse(response, request, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."))
                )

                // OAuth2 로그인 설정 추가
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo    // 사용자 정보를 처리할 서비스 지정
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)    // 인증 성공 후 처리할 핸들러 지정
                        // 실패 핸들러 추가(디버깅용)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 로그인 실패", exception);

                            // 세션 정보 상세 로깅
                            HttpSession session = request.getSession(false);
                            if (session != null) {
                                log.error("세션 정보 - ID: {}, CreationTime: {}, LastAccessedTime: {}", session.getId(), new Date(session.getCreationTime()), new Date(session.getLastAccessedTime()));
                            } else {
                                log.error("세션을 찾을 수 없습니다");
                            }

                            String encodedMessage = URLEncoder.encode("OAuth2 인증 실패: " + exception.getMessage(), StandardCharsets.UTF_8);

                            response.sendRedirect(frontendUrl + "/login?error=" + encodedMessage);
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        // Swagger 관련 경로 - 모든 HTTP 메서드 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()


                        // 인가(로그인) 없이 접근 가능한 API
                        .requestMatchers(HttpMethod.POST,
                                "/v1/auth/signup",
                                "/v1/auth/login",
                                "/v1/auth/refresh",
                                "/v1/auth/oauth2/token/exchange").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/v1/closets/public",
                                "/v1/closets/{closetId}",  // 추가 추천
                                // "/v1/sale-posts",
                                // "/v1/sale-posts/{salePostId}",
                                "/v1/categories").permitAll()

                        // 소셜 로그인
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // Admin
                        .requestMatchers("/admin/**").hasAuthority(UserRole.Authority.ADMIN)

                        // Monitor
                        .requestMatchers("/actuator/info", "/actuator/health", "/actuator/prometheus").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .build();
    }

    // 에러 응답을 JSON 형태로 작성하는 유틸리티 메서드
    private void writeErrorResponse(
            HttpServletResponse response,
            HttpServletRequest request,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new LinkedHashMap<>();    // HashMap은 키 순서를 보장하지 않음. 변경
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("httpStatus", status.name());
        errorResponse.put("statusValue", status.value());
        errorResponse.put("success", false);
        errorResponse.put("code", status == HttpStatus.UNAUTHORIZED ? "AUTHENTICATION_ERROR" : "ACCESS_DENIED");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}