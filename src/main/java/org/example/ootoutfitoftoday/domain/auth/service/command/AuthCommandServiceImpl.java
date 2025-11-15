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
import org.example.ootoutfitoftoday.domain.auth.dto.response.DeviceInfoResponse;
import org.example.ootoutfitoftoday.domain.auth.enums.LoginType;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.auth.repository.RedisRefreshTokenRepository;
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

    // Redis 키 접두사
    private static final String REDIS_KEY_PREFIX = "oauth:temp:code:";

    // 락 키 접두사 추가
    private static final String USER_LOCK_PREFIX = "auth:user:lock:";

    // Redis 분산 락을 위한 Redisson 클라이언트 추가
    private final RedissonClient redissonClient;
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final ChatParticipatingUserQueryService chatParticipatingUserQueryService;
    private final ChatReferenceToChatroomCommandService chatReferenceToChatroomCommandService;
    // MySQL 리포지토리 제거, Redis 리포지토리로 대체
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;
    // private final RefreshTokenRepository refreshTokenRepository;

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 사용자당 최대 디바이스 수 설정(application.yml에서 주입)
    @Value("${jwt.max-devices-per-user:5}")
    private int maxDevicesPerUser;

    // 회원가입
    // TODO: 리팩토링 고려
    @Override
    public void signup(AuthSignupRequest request) {

        if (userQueryService.existsByLoginId(request.getLoginId())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (userQueryService.existsByEmail(request.getEmail())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }
        if (userQueryService.existsByNickname(request.getNickname())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_NICKNAME);
        }
        if (userQueryService.existsByPhoneNumber(request.getPhoneNumber())) {
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

    // 로그인
    // 액세스 토큰, 리프레시 토큰 모두 응답 바디로 발급
    // 분산 락 적용
    @Override
    public AuthLoginResponse login(AuthLoginRequest request, HttpServletRequest httpRequest) {

        // 락 밖에서
        // 캐시된 DTO로 사용자 조회
        UserCacheDto cachedUser = userQueryService.findCachedByLoginId(request.getLoginId());

        // 삭제된 사용자 체크
        if (cachedUser.isDeleted()) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), cachedUser.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        // 사용자별 분산 락 획득
        String lockKey = USER_LOCK_PREFIX + cachedUser.getId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (!acquired) {
                log.warn("로그인 락 획득 실패 - userId: {}", cachedUser.getId());
                throw new AuthException(AuthErrorCode.CONCURRENT_LOGIN_IN_PROGRESS);
            }

            log.info("로그인 락 획득 성공 - userId: {}", cachedUser.getId());

            // 락 보호 영역 - Entity가 필요한 경우에만 조회
            User user = userQueryService.findByIdAndIsDeletedFalse(cachedUser.getId());

            // 세션 관리 로직 수행
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

    // 락 보호 영역에서 실행될 실제 로그인 로직
    private AuthLoginResponse performLoginWithLock(
            User user,
            AuthLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        // Redis에서 현재 디바이스 수 조회
        long deviceCount = redisRefreshTokenRepository.countByUserId(user.getId());

        log.info("현재 활성 디바이스 수: {} (최대: {})", deviceCount, maxDevicesPerUser);

        // 최대 디바이스 수 초과 시 가장 오래된 디바이스 삭제
        if (deviceCount >= maxDevicesPerUser) {
            // 가장 오래된 디바이스 삭제
            Optional<DeviceInfoResponse> oldestDeviceOpt = redisRefreshTokenRepository.findOldestDeviceByUserId(user.getId());

            if (oldestDeviceOpt.isPresent()) {
                DeviceInfoResponse oldestDevice = oldestDeviceOpt.get();
                redisRefreshTokenRepository.deleteByUserIdAndDeviceId(user.getId(), oldestDevice.getDeviceId());
                log.info("최대 디바이스 수 초과로 가장 오래된 디바이스 삭제 - userId: {}, deviceId: {}", user.getId(), oldestDevice.getDeviceId());
            }
        }

        // 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole());

        // 리프레시 토큰 생성
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        // Redis에 리프레시 토큰 저장
        saveOrUpdateRefreshToken(user, request.getDeviceId(), request.getDeviceName(), refreshToken, httpRequest);

        log.info("로그인 완료 - userId: {}, deviceId: {}", user.getId(), request.getDeviceId());

        return new AuthLoginResponse(accessToken, refreshToken);
    }

    // 토큰 재발급(액세스 토큰 만료 시 클라이언트가 저장해둔 리프레시 토큰으로 새 액세스 토큰 발급)
    // 바디로 전달받은 리프레시 토큰을 파라미터로 받음
    @Override
    public AuthLoginResponse refresh(
            String refreshToken,
            String deviceId,
            HttpServletRequest httpRequest
    ) {
        // 리프레시 토큰 타입 검증 추가
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN_TYPE);
        }

        // 리프레시 토큰 만료 확인
        if (jwtUtil.isExpired(refreshToken)) {
            throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // Redis에서 토큰 존재 여부 확인
        if (!redisRefreshTokenRepository.existsByToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Redis에서 userId 조회
        Long userId = redisRefreshTokenRepository.findUserIdByToken(refreshToken)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // Redis에서 deviceId 조회
        String storedDeviceId = redisRefreshTokenRepository.findDeviceIdByToken(refreshToken).orElseThrow(
                () -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // 디바이스 ID 검증(보안 강화)
        if (!storedDeviceId.equals(deviceId)) {
            log.warn("사용자 Device ID 불일치: userId={}, stored: {}, requested: {}", userId, storedDeviceId, deviceId);
            throw new AuthException(AuthErrorCode.DEVICE_MISMATCH);
        }

        // 토큰 메타데이터 조회하여 만료 확인
        DeviceInfoResponse tokenMetadata = redisRefreshTokenRepository.findTokenMetadata(refreshToken).orElseThrow(
                () -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (tokenMetadata.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 만료된 토큰 삭제
            redisRefreshTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
            throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // User 조회
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);


        // 새로운 액세스 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(userId, user.getRole());

        // 새로운 리프레시 토큰 생성(RTR)
        // RTR(Refresh Token Rotation): 보안을 위해 리프레시 토큰도 재사용하지 않고 폐기 & 새로 발급
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId());

        LocalDateTime newExpiresAt = jwtUtil.calculateRefreshTokenExpiresAt();

        // HttpServletRequest에서 메타데이터 추출
        String ipAddress = HttpRequestUtil.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // updateToken 호출 시 lastUsedAt도 자동 갱신됨
        // Redis에서 토큰 업데이트 (RTR)
        redisRefreshTokenRepository.updateToken(
                userId,
                deviceId,
                refreshToken,       // 기존 토큰
                newRefreshToken,    // 새 토큰
                newExpiresAt,
                ipAddress,
                userAgent
        );

        log.info("토큰 재발급 완료 - userId: {}, deviceId: {}", userId, deviceId);

        return new AuthLoginResponse(newAccessToken, newRefreshToken);
    }

    // OAuth2 임시 코드를 JWT 토큰으로 교환
    // 임시 코드는 3분간 유효하며 1회용
    // Redis에서 토큰 정보 조회 후 삭제
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

        // Redis에서 임시 코드로 토큰 정보 조회
        String redisKey = REDIS_KEY_PREFIX + code;
        String tokenJson = redisTemplate.opsForValue().get(redisKey);

        // 코드가 없거나 만료된 경우
        if (tokenJson == null) {
            log.warn("유효하지 않거나 만료된 임시 코드 - code: {}", code);
            throw new AuthException(AuthErrorCode.INVALID_OR_EXPIRED_CODE);
        }

        try {
            // JSON 파싱하여 토큰 정보 추출
            Map<String, String> tokenData = objectMapper.readValue(tokenJson, Map.class);

            String accessToken = tokenData.get("accessToken");
            String refreshToken = tokenData.get("refreshToken");
            String userId = tokenData.get("userId");

            log.info("토큰 정보 파싱 완료 - userId: {}", userId);

            // 디바이스 정보로 RefreshToken 저장
            User user = userQueryService.findByIdAndIsDeletedFalse(Long.parseLong(userId));

            LocalDateTime expiresAt = jwtUtil.calculateRefreshTokenExpiresAt();

            // Redis에서 디바이스 수 확인
            long deviceCount = redisRefreshTokenRepository.countByUserId(user.getId());

            if (deviceCount >= maxDevicesPerUser) {
                // 가장 오래된 디바이스 삭제
                Optional<DeviceInfoResponse> oldestDeviceOpt = redisRefreshTokenRepository.findOldestDeviceByUserId(user.getId());

                if (oldestDeviceOpt.isPresent()) {
                    DeviceInfoResponse oldestDevice = oldestDeviceOpt.get();
                    redisRefreshTokenRepository.deleteByUserIdAndDeviceId(user.getId(), oldestDevice.getDeviceId());
                    log.info("최대 디바이스 수 초과로 가장 오래된 디바이스 삭제: userId={}, deviceId={}", user.getId(), oldestDevice.getDeviceId());
                }
            }

            String ipAddress = HttpRequestUtil.getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            log.info("=== 클라이언트 정보 추출 ===");
            log.info("IP Address: {}", ipAddress);
            log.info("User-Agent: {}", userAgent);

            // Redis에 리프레시 토큰 저장
            redisRefreshTokenRepository.save(
                    user.getId(),
                    deviceId,
                    deviceName,
                    refreshToken,
                    expiresAt,
                    ipAddress,
                    userAgent
            );

            log.info("Refresh Token 저장 완료 - userId: {}, deviceId: {}", userId, deviceId);

            // Redis에서 임시 코드(1회용) 삭제
            redisTemplate.delete(redisKey);
            log.info("임시 코드(1회용) 삭제 완료 - code: {}", code);
            log.info("OAuth2 토큰 교환 성공 - userId: {}", userId);

            return new AuthLoginResponse(accessToken, refreshToken);

        } catch (Exception e) {
            log.error("토큰 교환 중 오류 발생 - code: {}", code, e);
            throw new AuthException(AuthErrorCode.TOKEN_EXCHANGE_FAILED);
        }
    }

    // 로그아웃
    // DB에서 리프레시 토큰 삭제
    // deviceId 파라미터 추가 -> 특정 디바이스만 로그아웃
    @Override
    public void logout(AuthUser authUser, String deviceId) {

        User user = userQueryService.findByIdAndIsDeletedFalse(authUser.getUserId());

        // Redis에서 리프레시 토큰 삭제
        redisRefreshTokenRepository.deleteByUserIdAndDeviceId(user.getId(), deviceId);

        log.info("로그아웃 완료 - userId: {}, deviceId: {}", user.getId(), deviceId);
    }

    // 모든 디바이스에서 로그아웃
    // 분산 락 추가
    @Override
    public void logoutAll(AuthUser authUser) {

        // Redis에서 사용자의 모든 디바이스 삭제
        redisRefreshTokenRepository.deleteAllByUserId(authUser.getUserId());

        log.info("전체 로그아웃 완료 - userId: {}", authUser.getUserId());
    }

    // 특정 디바이스 강제 제거
    @Override
    public void removeDevice(
            AuthUser authUser,
            String deviceId,
            String currentDeviceId
    ) {
        // 현재 로그인한 디바이스 제거 시도 차단
        if (deviceId.equals(currentDeviceId)) {
            throw new AuthException(AuthErrorCode.CANNOT_REMOVE_CURRENT_DEVICE);
        }

        // Redis에서 해당 디바이스 존재 여부 확인
        Optional<String> tokenOpt = redisRefreshTokenRepository.findByUserIdAndDeviceId(authUser.getUserId(), deviceId);

        if (tokenOpt.isEmpty()) {
            throw new AuthException(AuthErrorCode.DEVICE_NOT_FOUND);
        }

        // Redis에서 디바이스 삭제
        redisRefreshTokenRepository.deleteByUserIdAndDeviceId(authUser.getUserId(), deviceId);

        log.info("디바이스 강제 제거 완료 - userId: {}, deviceId: {}", authUser.getUserId(), deviceId);
    }

    // 회원탈퇴
    @Override
    public void withdraw(AuthWithdrawRequest request, AuthUser authUser) {

        User user = userQueryService.findByIdAndIsDeletedFalse(authUser.getUserId());

        // 일반 회원만 비밀번호 검증
        if (user.getLoginType() == LoginType.LOGIN_ID) {
            // 비밀번호가 제공되지 않은 경우 예외 발생
            // 민감 작업이므로 보안을 위해 null과 불일치 동일하게 처리
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
            }
        }
        // 소셜 회원은 비밀번호 검증 없이 바로 탈퇴 처리

        // Redis에서 리프레시 토큰 삭제
        redisRefreshTokenRepository.deleteAllByUserId(user.getId());

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
    }

    // 리프레시 토큰 저장 또는 업데이트
    private void saveOrUpdateRefreshToken(
            User user,
            String deviceId,
            String deviceName,
            String refreshToken,
            HttpServletRequest httpRequest
    ) {
        LocalDateTime expiresAt = jwtUtil.calculateRefreshTokenExpiresAt();

        // 클라이언트 IP 추출
        String ipAddress = HttpRequestUtil.getClientIp(httpRequest);

        // User-Agent 추출
        String userAgent = httpRequest.getHeader("User-Agent");

        // Redis에서 기존 토큰 확인
        Optional<String> existingTokenOpt = redisRefreshTokenRepository.findByUserIdAndDeviceId(user.getId(), deviceId);

        if (existingTokenOpt.isPresent()) {
            // 기존 토큰이 있으면 업데이트 (RTR)
            String existingToken = existingTokenOpt.get();
            redisRefreshTokenRepository.updateToken(
                    user.getId(),
                    deviceId,
                    existingToken,
                    refreshToken,
                    expiresAt,
                    ipAddress,
                    userAgent
            );

            log.debug("기존 디바이스 토큰 업데이트 - userId: {}, deviceId: {}", user.getId(), deviceId);
        } else {
            // 없으면 새로 저장
            redisRefreshTokenRepository.save(user.getId(), deviceId, deviceName, refreshToken, expiresAt, ipAddress, userAgent);

            log.debug("새 디바이스 토큰 저장 - userId: {}, deviceId: {}", user.getId(), deviceId);
        }
    }
}