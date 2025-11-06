package org.example.ootoutfitoftoday.domain.user.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.enums.LoginType;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserGetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.exception.UserErrorCode;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
import org.example.ootoutfitoftoday.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean existsByLoginId(String loginId) {

        return userRepository.existsByLoginId(loginId);
    }

    @Override
    public boolean existsByEmail(String email) {

        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {

        return userRepository.existsByNickname(nickname);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {

        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public User findByLoginIdAndIsDeletedFalse(String loginId) {

        return userRepository.findByLoginIdAndIsDeletedFalse(loginId).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND)
        );
    }

    @Override
    public User findByIdAndIsDeletedFalse(Long id) {

        return userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND)
        );
    }

    @Override
    public User findByEmailAndIsDeletedFalse(String email) {

        return userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND)
        );
    }

    @Override
    public Optional<User> findBySocialProviderAndSocialId(SocialProvider provider, String socialId) {

        return userRepository.findBySocialProviderAndSocialId(provider, socialId);
    }

    @Override
    public UserGetMyInfoResponse getMyInfo(Long id) {

        User user = userRepository.findByIdAsNativeQuery(id);

        return UserGetMyInfoResponse.from(user);
    }

    @Override
    public void verifyPassword(UserPasswordVerificationRequest request, AuthUser authUser) {

        User user = findByIdAndIsDeletedFalse(authUser.getUserId());

        // 소셜 로그인 사용자는 비밀번호 검증 스킵(성공)
        if (user.getLoginType() == LoginType.SOCIAL) {

            return;
        }

        // 일반 유저는 비밀번호 필수
        // 비민감 작업이므로 명확한 에러 메시지
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new AuthException(AuthErrorCode.VALIDATION_FAILED);
        }

        // 일반 회원만 비밀번호 검증 진행
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }
    }

    @Override
    public int countAllUsers() {

        return userRepository.countAllUsers();
    }

    @Override
    public int countByIsDeleted(Boolean isDeleted) {

        return userRepository.countByIsDeleted(isDeleted);
    }

    @Override
    public int countUsersRegisteredSince(LocalDateTime start, LocalDateTime end) {

        return userRepository.countUsersRegisteredSince(start, end);
    }

    @Override
    public User findByIdAsNativeQuery(Long userId) {

        return userRepository.findByIdAsNativeQuery(userId);
    }
}
