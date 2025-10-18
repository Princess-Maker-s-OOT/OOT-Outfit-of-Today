package org.example.ootoutfitoftoday.domain.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.example.ootoutfitoftoday.domain.user.service.UserService;
import org.example.ootoutfitoftoday.security.jwt.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void signup(AuthSignupRequest request) {

        if (userService.existsByLoginId(request.getLoginId())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (userService.existsByEmail(request.getEmail())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }
        if (userService.existsByNickname(request.getNickname())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_NICKNAME);
        }
        if (userService.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_PHONE_NUMBER);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .loginId(request.getLoginId())
                .email(request.getEmail())
                .nickname(request.getNickname())
                .username(request.getUsername())
                .password(encodedPassword)
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.ROLE_USER)
                .build();

        userService.save(user);
    }
}
