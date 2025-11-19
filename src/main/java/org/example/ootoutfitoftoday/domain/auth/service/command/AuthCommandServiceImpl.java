package org.example.ootoutfitoftoday.domain.auth.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.util.DefaultLocationConstants;
import org.example.ootoutfitoftoday.common.util.HttpRequestUtil;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthLoginRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthWithdrawRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.response.AuthLoginResponse;
import org.example.ootoutfitoftoday.domain.auth.entity.RefreshToken;
import org.example.ootoutfitoftoday.domain.auth.enums.LoginType;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.auth.repository.RefreshTokenRepository;
import org.example.ootoutfitoftoday.domain.chat.service.command.ChatReferenceToChatroomCommandService;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query.ChatParticipatingUserQueryService;
import org.example.ootoutfitoftoday.domain.user.dto.UserCacheDto;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.example.ootoutfitoftoday.domain.user.exception.UserErrorCode;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
import org.example.ootoutfitoftoday.domain.user.service.command.UserCommandService;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.example.ootoutfitoftoday.security.jwt.JwtUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandServiceImpl implements AuthCommandService {

    private static final String REDIS_KEY_PREFIX = "oauth:temp:code:";

    private static final String USER_LOCK_PREFIX = "auth:user:lock:";

    private final RedissonClient redissonClient;

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final ChatParticipatingUserQueryService chatParticipatingUserQueryService;
    private final ChatReferenceToChatroomCommandService chatReferenceToChatroomCommandService;
    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${jwt.max-devices-per-user:5}")
    private int maxDevicesPerUser;

    @Override
    public void signup(AuthSignupRequest request) {
        if (userQueryService.existsByLoginId(request.getLoginId())) {
            log.warn("회원가입 실패 - 로그인 ID 중복 - loginId: {}", request.getLoginId());
            throw new AuthException(AuthErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (userQueryService.existsByEmail(request.getEmail())) {
            log.warn("회원가입 실패 - 이메일 중복 - email: {}", request.getEmail());
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }
        if (userQueryService.existsByNickname(request.getNickname())) {
            log.warn("회원가입 실패 - 닉네임 중복 - nickname: {}", request.getNickname());
            throw new AuthException(AuthErrorCode.DUPLICATE_NICKNAME);
        }
        if (userQueryService.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("회원가입 실패 - 전화번호 중복 - phoneNumber: {}", request.getPhoneNumber());
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
                .tradeAddress(DefaultLocationConstants.DEFAULT_TRADE_ADDRESS)
                .tradeLocation(DefaultLocationConstants.DEFAULT_TRADE_LOCATION)
                .build();

        userCommandService.save(user);
    }

    @Override
    public AuthLoginResponse login(AuthLoginRequest request, HttpServletRequest httpRequest) {
        UserCacheDto cachedUser = userQueryService.findCachedByLoginId(request.getLoginId());

        if (cachedUser.isDeleted()) {
            log.warn("로그인 실패 - 삭제된 사용자 - loginId: {}", request.getLoginId());
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getPassword(), cachedUser.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 불일치 - loginId: {}", request.getLoginId());
            throw new AuthException(AuthErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        String lockKey = USER_LOCK_PREFIX + cachedUser.getId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(2, TimeUnit.SECONDS);

            if (!acquired) {
                log.warn("로그인 실패 - 락 획득 실패 - userId: {}", cachedUser.getId());
                throw new AuthException(AuthErrorCode.CONCURRENT_LOGIN_IN_PROGRESS);
            }

            log.info("로그인 락 획득 성공 - userId: {}", cachedUser.getId());

            User user = userQueryService.findByIdAndIsDeletedFalse(cachedUser.getId());

            return performLoginWithLock(user, request, httpRequest);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("로그인 처리 중 인터럽트 발생 - userId: {}", cachedUser.getId(), e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.", e);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("로그인 락 해제 - userId: {}", cachedUser.getId());
            }
        }
    }

    private AuthLoginResponse performLoginWithLock(
            User user,
            AuthLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        long deviceCount = refreshTokenRepository.countByUserId(user.getId());

        log.info("현재 활성 디바이스 수: {} (최대: {})", deviceCount, maxDevicesPerUser);

        if (deviceCount >= maxDevicesPerUser) {
            Optional<RefreshToken> oldestDeviceOpt = refreshTokenRepository.findTopByUserIdOrderByLastUsedAtAsc(user.getId());

            if (oldestDeviceOpt.isPresent()) {
                RefreshToken oldestDevice = oldestDeviceOpt.get();
                refreshTokenRepository.deleteByUserIdAndDeviceId(user.getId(), oldestDevice.getDeviceId());
                log.info("최대 디바이스 수 초과로 가장 오래된 디바이스 삭제 - userId: {}, deviceId: {}", user.getId(), oldestDevice.getDeviceId());
            }
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        saveOrUpdateRefreshToken(user, request.getDeviceId(), request.getDeviceName(), refreshToken, httpRequest);

        log.info("로그인 완료 - userId: {}, deviceId: {}", user.getId(), request.getDeviceId());

        return new AuthLoginResponse(accessToken, refreshToken);
    }

    @Override
    public AuthLoginResponse refresh(
            String refreshToken,
            String deviceId,
            HttpServletRequest httpRequest
    ) {
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("토큰 재발급 실패 - 잘못된 토큰 타입 - deviceId: {}", deviceId);
            throw new AuthException(AuthErrorCode.INVALID_TOKEN_TYPE);
        }
        if (jwtUtil.isExpired(refreshToken)) {
            log.warn("토큰 재발급 실패 - 리프레시 토큰 만료 - deviceId: {}", deviceId);
            throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> {
            log.warn("토큰 재발급 실패 - 유효하지 않은 리프레시 토큰 - deviceId: {}", deviceId);

            return new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        });

        if (!storedToken.getDeviceId().equals(deviceId)) {
            log.warn("토큰 재발급 실패 - 디바이스 ID 불일치 - userId: {}, storedDeviceId: {}, requestedDeviceId: {}", storedToken.getUser().getId(), storedToken.getDeviceId(), deviceId);
            throw new AuthException(AuthErrorCode.DEVICE_MISMATCH);
        }
        if (!storedToken.isValid(LocalDateTime.now())) {
            log.warn("토큰 재발급 실패 - 리프레시 토큰 만료 - userId: {}, deviceId: {}", storedToken.getUser().getId(), deviceId);
            refreshTokenRepository.delete(storedToken);
            throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        Long userId = storedToken.getUser().getId();
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        String newAccessToken = jwtUtil.createAccessToken(userId, user.getRole());
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId());

        LocalDateTime newExpiresAt = jwtUtil.calculateRefreshTokenExpiresAt();

        String ipAddress = HttpRequestUtil.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        storedToken.updateToken(newRefreshToken, newExpiresAt, ipAddress, userAgent);

        log.info("토큰 재발급 완료 - userId: {}, deviceId: {}", userId, deviceId);

        return new AuthLoginResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public AuthLoginResponse exchangeOAuthToken(
            String code,
            String deviceId,
            String deviceName,
            HttpServletRequest httpRequest
    ) {
        log.info("=== OAuth2 임시 코드 교환 시작 ===");
        log.info("Code: {}", code);
        log.info("Device ID: {}", deviceId);
        log.info("Device Name: {}", deviceName);

        String redisKey = REDIS_KEY_PREFIX + code;
        String tokenJson = redisTemplate.opsForValue().get(redisKey);

        if (tokenJson == null) {
            log.warn("OAuth 토큰 교환 실패 - 유효하지 않거나 만료된 임시 코드 - code: {}", code);
            throw new AuthException(AuthErrorCode.INVALID_OR_EXPIRED_CODE);
        }

        try {
            Map<String, String> tokenData = objectMapper.readValue(tokenJson, Map.class);

            String accessToken = tokenData.get("accessToken");
            String refreshToken = tokenData.get("refreshToken");
            String userId = tokenData.get("userId");

            log.info("토큰 정보 파싱 완료 - userId: {}", userId);

            User user = userQueryService.findByIdAndIsDeletedFalse(Long.parseLong(userId));

            String lockKey = USER_LOCK_PREFIX + user.getId();
            RLock lock = redissonClient.getLock(lockKey);

            try {
                boolean acquired = lock.tryLock(2, TimeUnit.SECONDS);

                if (!acquired) {
                    log.warn("OAuth 토큰 교환 실패 - 락 획득 실패 - userId: {}", user.getId());
                    throw new AuthException(AuthErrorCode.CONCURRENT_LOGIN_IN_PROGRESS);
                }

                log.info("OAuth 토큰 교환 락 획득 성공 - userId: {}", user.getId());

                return performOAuthTokenExchangeWithLock(
                        user,
                        deviceId,
                        deviceName,
                        refreshToken,
                        accessToken,
                        httpRequest,
                        code,
                        redisKey
                );

            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("OAuth 토큰 교환 락 해제 - userId: {}", user.getId());
                }
            }

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("OAuth 토큰 교환 중 오류 발생 - code: {}", code, e);
            throw new AuthException(AuthErrorCode.TOKEN_EXCHANGE_FAILED);
        }
    }

    private AuthLoginResponse performOAuthTokenExchangeWithLock(
            User user,
            String deviceId,
            String deviceName,
            String refreshToken,
            String accessToken,
            HttpServletRequest httpRequest,
            String code,
            String redisKey
    ) {
        long deviceCount = refreshTokenRepository.countByUserId(user.getId());

        if (deviceCount >= maxDevicesPerUser) {
            Optional<RefreshToken> oldestDeviceOpt = refreshTokenRepository.findTopByUserIdOrderByLastUsedAtAsc(user.getId());

            if (oldestDeviceOpt.isPresent()) {
                RefreshToken oldestDevice = oldestDeviceOpt.get();
                refreshTokenRepository.deleteByUserIdAndDeviceId(user.getId(), oldestDevice.getDeviceId());
                log.info("최대 디바이스 수 초과로 가장 오래된 디바이스 삭제: userId={}, deviceId={}", user.getId(), oldestDevice.getDeviceId());
            }
        }

        saveOrUpdateRefreshToken(user, deviceId, deviceName, refreshToken, httpRequest);

        log.info("Refresh Token 저장 완료 - userId: {}, deviceId: {}", user.getId(), deviceId);

        redisTemplate.delete(redisKey);
        log.info("임시 코드(1회용) 삭제 완료 - code: {}", code);
        log.info("OAuth2 토큰 교환 성공 - userId: {}", user.getId());

        return new AuthLoginResponse(accessToken, refreshToken);
    }

    @Override
    public void logout(AuthUser authUser, String deviceId) {
        String lockKey = USER_LOCK_PREFIX + authUser.getUserId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(1, TimeUnit.SECONDS);

            if (!acquired) {
                log.warn("로그아웃 실패 - 락 획득 실패 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);
                throw new AuthException(AuthErrorCode.LOGOUT_IN_PROGRESS);
            }


            log.info("로그아웃 락 획득 성공 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);

            refreshTokenRepository.deleteByUserIdAndDeviceId(authUser.getUserId(), deviceId);

            log.info("로그아웃 완료 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("로그아웃 처리 중 인터럽트 발생 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId, e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("로그아웃 락 해제 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);
            }
        }
    }

    @Override
    public void logoutAll(AuthUser authUser) {
        String lockKey = USER_LOCK_PREFIX + authUser.getUserId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(2, TimeUnit.SECONDS);

            if (!acquired) {
                log.warn("전체 로그아웃 실패 - 락 획득 실패 - userId: {}", authUser.getUserId());
                throw new AuthException(AuthErrorCode.LOGOUT_IN_PROGRESS);
            }

            log.info("전체 로그아웃 락 획득 성공 - userId: {}", authUser.getUserId());

            refreshTokenRepository.deleteByUserId(authUser.getUserId());

            log.info("전체 로그아웃 완료 - userId: {}", authUser.getUserId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("전체 로그아웃 처리 중 인터럽트 발생 - userId: {}", authUser.getUserId(), e);
            throw new RuntimeException("전체 로그아웃 처리 중 오류가 발생했습니다.", e);

        } finally {

            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("전체 로그아웃 락 해제 - userId: {}", authUser.getUserId());
            }
        }
    }

    @Override
    public void removeDevice(
            AuthUser authUser,
            String deviceId,
            String currentDeviceId
    ) {
        if (deviceId.equals(currentDeviceId)) {
            log.warn("디바이스 제거 실패 - 현재 디바이스 제거 시도 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);
            throw new AuthException(AuthErrorCode.CANNOT_REMOVE_CURRENT_DEVICE);
        }

        String lockKey = USER_LOCK_PREFIX + authUser.getUserId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(1, TimeUnit.SECONDS);

            if (!acquired) {
                log.warn("디바이스 제거 실패 - 락 획득 실패 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);
                throw new AuthException(AuthErrorCode.DEVICE_REMOVAL_IN_PROGRESS);
            }

            log.info("디바이스 제거 락 획득 성공 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);

            RefreshToken token = refreshTokenRepository.findByUserIdAndDeviceId(authUser.getUserId(), deviceId).orElseThrow(() -> {
                log.warn("디바이스 제거 실패 - 디바이스를 찾을 수 없음 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);

                return new AuthException(AuthErrorCode.DEVICE_NOT_FOUND);
            });

            refreshTokenRepository.delete(token);

            log.info("디바이스 강제 제거 완료 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("디바이스 제거 처리 중 인터럽트 발생 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId, e);
            throw new RuntimeException("디바이스 제거 처리 중 오류가 발생했습니다.", e);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("디바이스 제거 락 해제 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);
            }
        }
    }

    @Override
    public void withdraw(AuthWithdrawRequest request, AuthUser authUser) {
        User user = userQueryService.findByIdAndIsDeletedFalse(authUser.getUserId());

        if (user.getLoginType() == LoginType.LOGIN_ID) {
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                log.warn("회원탈퇴 실패 - 비밀번호 누락 - userId: {}", authUser.getUserId());
                throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("회원탈퇴 실패 - 비밀번호 불일치 - userId: {}", authUser.getUserId());
                throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
            }
        }

        String lockKey = USER_LOCK_PREFIX + authUser.getUserId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(3, TimeUnit.SECONDS);

            if (!acquired) {
                log.warn("회원탈퇴 실패 - 락 획득 실패 - userId: {}", authUser.getUserId());
                throw new AuthException(AuthErrorCode.WITHDRAWAL_IN_PROGRESS);
            }

            log.info("회원탈퇴 락 획득 성공 - userId: {}", authUser.getUserId());

            refreshTokenRepository.deleteByUserId(user.getId());

            List<ChatParticipatingUser> chatParticipatingUsers = chatParticipatingUserQueryService.getChatParticipatingUsers(user);

            chatParticipatingUsers
                    .forEach(chatParticipatingUser1 -> {
                        List<ChatParticipatingUser> usersInChatroom = chatParticipatingUserQueryService.getAllParticipatingUserByChatroom(chatParticipatingUser1.getChatroom());
                        usersInChatroom
                                .forEach(chatParticipatingUser2 -> {
                                    if (!Objects.equals(chatParticipatingUser2.getUser(), user) &&
                                            chatParticipatingUser2.isDeleted()) {
                                        chatReferenceToChatroomCommandService.deleteChats(chatParticipatingUser2.getChatroom().getId());
                                    }
                                });
                    });

            userCommandService.softDeleteUser(user);

            log.info("회원탈퇴 완료 - userId: {}", user.getId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("회원탈퇴 처리 중 인터럽트 발생 - userId: {}", authUser.getUserId(), e);
            throw new RuntimeException("회원탈퇴 처리 중 오류가 발생했습니다.", e);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("회원탈퇴 락 해제 - userId: {}", authUser.getUserId());
            }
        }
    }

    private void saveOrUpdateRefreshToken(
            User user,
            String deviceId,
            String deviceName,
            String refreshToken,
            HttpServletRequest httpRequest
    ) {
        LocalDateTime newExpiresAt = jwtUtil.calculateRefreshTokenExpiresAt();

        String ipAddress = HttpRequestUtil.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        refreshTokenRepository.findByUserIdAndDeviceId(user.getId(), deviceId)
                .ifPresentOrElse(
                        existingToken -> existingToken.updateToken(refreshToken, newExpiresAt, ipAddress, userAgent),
                        () -> {
                            RefreshToken newToken = RefreshToken.create(
                                    user,
                                    deviceId,
                                    deviceName,
                                    refreshToken,
                                    newExpiresAt,
                                    ipAddress,
                                    userAgent
                            );
                            refreshTokenRepository.save(newToken);
                        }
                );
    }
}