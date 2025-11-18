package org.example.ootoutfitoftoday.domain.user.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.enums.LoginType;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.user.dto.UserCacheDto;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserGetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.exception.UserErrorCode;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
import org.example.ootoutfitoftoday.domain.user.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Cacheable(value = "userExistsCache", key = "'loginId:' + #loginId", unless = "#result == true")
    public boolean existsByLoginId(String loginId) {

        return userRepository.existsByLoginId(loginId);
    }

    @Override
    @Cacheable(value = "userExistsCache", key = "'email:' + #email", unless = "#result == true")
    public boolean existsByEmail(String email) {

        return userRepository.existsByEmail(email);
    }

    @Override
    @Cacheable(value = "userExistsCache", key = "'nickname:' + #nickname", unless = "#result == true")
    public boolean existsByNickname(String nickname) {

        return userRepository.existsByNickname(nickname);
    }

    @Override
    @Cacheable(value = "userExistsCache", key = "'phoneNumber:' + #phoneNumber", unless = "#result == true")
    public boolean existsByPhoneNumber(String phoneNumber) {

        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public User findByLoginIdAndIsDeletedFalse(String loginId) {

        return userRepository.findByLoginIdAndIsDeletedFalse(loginId).orElseThrow(() -> {
            log.warn("사용자를 찾을 수 없음 - loginId: {}", loginId);

            return new UserException(UserErrorCode.USER_NOT_FOUND);
        });
    }

    @Override
    public User findByIdAndIsDeletedFalse(Long id) {

        return userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> {
            log.warn("사용자를 찾을 수 없음 - userId: {}", id);

            return new UserException(UserErrorCode.USER_NOT_FOUND);
        });
    }

    @Override
    public User findByEmailAndIsDeletedFalse(String email) {

        return userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(() -> {
            log.warn("사용자를 찾을 수 없음 - email: {}", email);

            return new UserException(UserErrorCode.USER_NOT_FOUND);
        });
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

        if (user.getLoginType() == LoginType.SOCIAL) {

            return;
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            log.warn("비밀번호 검증 실패 - 비밀번호 누락 - userId: {}", authUser.getUserId());
            throw new AuthException(AuthErrorCode.VALIDATION_FAILED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("비밀번호 검증 실패 - 비밀번호 불일치 - userId: {}", authUser.getUserId());
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

    @Override
    public Page<Long> findAllActiveUserIds(Pageable pageable) {

        return userRepository.findAllActiveUserIds(pageable);
    }

    @Override
    @Cacheable(value = "userCache", key = "'loginId:' + #loginId", unless = "#result == null")
    public UserCacheDto findCachedByLoginId(String loginId) {
        User user = userRepository.findByLoginIdAndIsDeletedFalse(loginId).orElseThrow(() -> {
            log.warn("캐시된 사용자 조회 실패 - loginId: {}", loginId);

            return new UserException(UserErrorCode.USER_NOT_FOUND);
        });

        return UserCacheDto.from(user);
    }

    @Override
    @Cacheable(value = "userCache", key = "'id:' + #id", unless = "#result == null")
    public UserCacheDto findCachedById(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> {
            log.warn("캐시된 사용자 조회 실패 - userId: {}", id);

            return new UserException(UserErrorCode.USER_NOT_FOUND);
        });

        return UserCacheDto.from(user);
    }

    @Override
    @Cacheable(value = "userCache", key = "'email:' + #email", unless = "#result == null")
    public UserCacheDto findCachedByEmail(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(() -> {
            log.warn("캐시된 사용자 조회 실패 - email: {}", email);

            return new UserException(UserErrorCode.USER_NOT_FOUND);
        });

        return UserCacheDto.from(user);
    }
}