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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 회원가입 시 중복 체크 쿼리 캐싱
    // TTL 1분: 짧은 시간만 캐시하여 실시간성 보장
    @Override
    @Cacheable(value = "userExistsCache", key = "'loginId:' + #loginId", unless = "#result == true")
    public boolean existsByLoginId(String loginId) {

        return userRepository.existsByLoginId(loginId);
    }

    // 이메일 중복 체크 캐싱
    // unless = "#result == true": 이미 존재하는 경우(true)는 캐시하지 않음
    // 회원가입이 완료되면 새로운 이메일이므로 캐시 미스 발생 -> 의도된 동작
    @Override
    @Cacheable(value = "userExistsCache", key = "'email:' + #email", unless = "#result == true")
    public boolean existsByEmail(String email) {

        return userRepository.existsByEmail(email);
    }

    // 닉네임 중복 체크 캐싱
    @Override
    @Cacheable(value = "userExistsCache", key = "'nickname:' + #nickname", unless = "#result == true")
    public boolean existsByNickname(String nickname) {

        return userRepository.existsByNickname(nickname);
    }

    // 전화번호 중복 체크 캐싱
    @Override
    @Cacheable(value = "userExistsCache", key = "'phoneNumber:' + #phoneNumber", unless = "#result == true")
    public boolean existsByPhoneNumber(String phoneNumber) {

        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    // 로그인 시 사용자 조회 캐싱 (기존 유지)
    // 로그인은 빈번하게 발생하는 작업이므로 캐싱 효과 큼
    // TTL 10분: 사용자 정보가 변경되더라도 최대 10분 내 반영
    @Override
    @Cacheable(value = "userCache", key = "'loginId:' + #loginId", unless = "#result == null")
    public User findByLoginIdAndIsDeletedFalse(String loginId) {

        return userRepository.findByLoginIdAndIsDeletedFalse(loginId).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND)
        );
    }

    // ID로 사용자 조회 캐싱 추가
    // JWT 검증 후 사용자 정보 조회 시 캐시 활용
    // 토큰 갱신, API 호출 등에서 반복 조회되므로 성능 향상 효과 큼
    @Override
    @Cacheable(value = "userCache", key = "'id:' + #id", unless = "#result == null")
    public User findByIdAndIsDeletedFalse(Long id) {

        return userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND)
        );
    }

    // 이메일로 사용자 조회 캐싱 추가
    // OAuth2 로그인 시 이메일로 기존 계정 확인하는 작업 최적화
    @Override
    @Cacheable(value = "userCache", key = "'email:' + #email", unless = "#result == null")
    public User findByEmailAndIsDeletedFalse(String email) {

        return userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND)
        );
    }

    // 소셜 로그인 사용자 조회는 캐싱하지 않음
    // 소셜 로그인은 상대적으로 빈도가 낮고, 실시간 정보가 중요
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

    @Override
    public Page<Long> findAllActiveUserIds(Pageable pageable) {

        return userRepository.findAllActiveUserIds(pageable);
    }
}
