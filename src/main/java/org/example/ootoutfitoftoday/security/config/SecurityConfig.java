package org.example.ootoutfitoftoday.security.config;

import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.example.ootoutfitoftoday.security.filter.JwtAuthenticationFilter;
import org.example.ootoutfitoftoday.security.oauth2.CustomOAuth2UserService;
import org.example.ootoutfitoftoday.security.oauth2.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

@Configuration
//@RequiredArgsConstructor
@EnableWebSecurity  // Spring Security 활성화
@EnableMethodSecurity(securedEnabled = true)  // @Secured 활성화
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    // 순환참조 문제 발생 -> 해결을 위해 @Lazy(수동 생성자 필요) 사용
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Lazy OAuth2SuccessHandler oAuth2SuccessHandler,
            CustomOAuth2UserService customOAuth2UserService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, SecurityContextHolderAwareRequestFilter.class)    // JwtAuthenticationFilter를 스프링 시큐리티 인증 프로세스 전에 진행

                // JWT 사용 시 불필요한 기능들 비활성화
                .formLogin(AbstractHttpConfigurer::disable)      // [SSR] 서버가 로그인 HTML 폼 렌더링
                .anonymous(AbstractHttpConfigurer::disable)      // 미인증 사용자를 익명으로 처리
                .httpBasic(AbstractHttpConfigurer::disable)      // [SSR] 인증 팝업
                .logout(AbstractHttpConfigurer::disable)         // [SSR] 서버가 세션 무효화 후 리다이렉트
                .rememberMe(AbstractHttpConfigurer::disable)     // 서버가 쿠키 발급하여 자동 로그인

                // OAuth2 로그인 설정 추가
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo    // 사용자 정보를 처리할 서비스 지정
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)    // 인증 성공 후 처리할 핸들러 지정
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

                        // Actuator Health Check
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // 인증 없이 접근 가능한 API
                        .requestMatchers(HttpMethod.POST,
                                "/v1/auth/signup",
                                "/v1/auth/login",
                                "/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/v1/closets/public",
                                "/v1/closets/{closetId}",  // 추가 추천
                                "/v1/sale-posts",
                                "/v1/sale-posts/{salePostId}",
                                "/v1/categories",
                                "/api/oauth2/**",                      // OAuth2 URL 생성 API 추가
                                "/api/login/oauth2/**").permitAll()    // OAuth2 URL 생성 API 추가

                        // 소셜 로그인
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // Admin
                        .requestMatchers("/admin/**").hasAuthority(UserRole.Authority.ADMIN)

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .build();
    }
}