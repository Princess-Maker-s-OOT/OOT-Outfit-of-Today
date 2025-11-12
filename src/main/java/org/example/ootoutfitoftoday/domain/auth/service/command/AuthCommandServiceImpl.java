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
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.example.ootoutfitoftoday.domain.user.service.command.UserCommandService;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.example.ootoutfitoftoday.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandServiceImpl implements AuthCommandService {

    // 래디스 키 접두사
    private static final String REDIS_KEY_PREFIX = "oauth:temp:code:";

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final ChatParticipatingUserQueryService chatParticipatingUserQueryService;
    private final ChatReferenceToChatroomCommandService chatReferenceToChatroomCommandService;
    private final RefreshTokenRepository refreshTokenRepository;
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
    @Override
    public AuthLoginResponse login(AuthLoginRequest request, HttpServletRequest httpRequest) {

        long start = System.currentTimeMillis();

        // ⭐️사용자 조회
        long dbStart = System.currentTimeMillis();
        User user = userQueryService.findByLoginIdAndIsDeletedFalse(request.getLoginId());
        log.debug("[PERF] DB 조회: {} ms", System.currentTimeMillis() - dbStart);

        // ⭐️비밀번호 검증
        long pwStart = System.currentTimeMillis();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_LOGIN_CREDENTIALS);
        }
        log.debug("[PERF] 비밀번호 검증: {} ms", System.currentTimeMillis() - pwStart);

        // ⭐️기존 디바이스 정리
        long deviceStart = System.currentTimeMillis();
        // 단일 쿼리로 디바이스 수 확인 및 정리
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserIdOrderByLastUsedAtDesc(user.getId());

        if (tokens.size() >= maxDevicesPerUser) {
            // 가장 오래된 디바이스 삭제
            RefreshToken oldestToken = tokens.get(tokens.size() - 1);
            refreshTokenRepository.delete(oldestToken);
            log.info("최대 디바이스 수 초과로 가장 오래된 디바이스 삭제: userId={}, deviceId={}",
                    user.getId(), oldestToken.getDeviceId());
        }
        log.debug("[PERF] 디바이스 검증 및 정리: {} ms", System.currentTimeMillis() - deviceStart);

        // ⭐️JWT 발급
        long jwtStart = System.currentTimeMillis();
        // 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole());

        // 리프레시 토큰 생성 및 DB 저장
        String refreshToken = jwtUtil.createRefreshToken(user.getId());
        log.debug("[PERF] JWT 발급: {} ms", System.currentTimeMillis() - jwtStart);

        // ⭐️리프레시 토큰 저장
        long saveStart = System.currentTimeMillis();
        // 유저 정보와 함께 리프레시 토큰 저장
        saveOrUpdateRefreshToken(user, request.getDeviceId(), request.getDeviceName(), refreshToken, httpRequest);

        log.debug("[PERF] 토큰 저장: {} ms", System.currentTimeMillis() - saveStart);

        log.debug("[PERF] 전체 로그인 처리: {} ms", System.currentTimeMillis() - start);

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

        // DB에서 리프레시 토큰 조회
        // 탈취한 토큰인지, 로그아웃 및 회원탈퇴로 무효화된 토큰인지 확인
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow(
                () -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // 디바이스 ID 검증(보안 강화)
        if (!storedToken.getDeviceId().equals(deviceId)) {
            log.warn("사용자 Device ID 불일치: {}, stored: {}, requested: {}",
                    storedToken.getUser().getId(), storedToken.getDeviceId(), deviceId);
            throw new AuthException(AuthErrorCode.DEVICE_MISMATCH);
        }

        // 리프레시 토큰 유효성 확인
        if (!storedToken.isValid(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // userId로 User 조회
        Long userId = storedToken.getUser().getId();
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
        storedToken.updateToken(newRefreshToken, newExpiresAt, ipAddress, userAgent);

        return new AuthLoginResponse(newAccessToken, newRefreshToken);
    }

    // OAuth2 임시 코드를 JWT 토큰으로 교환
    // 임시 코드는 3분간 유효하며 1회용
    // 래디스에서 토큰 정보 조회 후 삭제
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

        // 래디스에서 임시 코드로 토큰 정보 조회
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

            // 멀티 디바이스 제한 확인
            List<RefreshToken> tokens = refreshTokenRepository.findAllByUserIdOrderByLastUsedAtDesc(user.getId());

            if (tokens.size() >= maxDevicesPerUser) {
                RefreshToken oldestToken = tokens.get(tokens.size() - 1);
                refreshTokenRepository.delete(oldestToken);
                log.info("최대 디바이스 수 초과로 가장 오래된 디바이스 삭제: userId={}, deviceId={}",
                        user.getId(), oldestToken.getDeviceId());
            }

            String ipAddress = HttpRequestUtil.getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            log.info("=== 클라이언트 정보 추출 ===");
            log.info("IP Address: {}", ipAddress);
            log.info("User-Agent: {}", userAgent);
            log.info("Remote Addr: {}", httpRequest.getRemoteAddr());
            log.info("X-Forwarded-For: {}", httpRequest.getHeader("X-Forwarded-For"));
            log.info("X-Real-IP: {}", httpRequest.getHeader("X-Real-IP"));

            // 디바이스별 토큰 저장(일반 로그인과 동일!)
            refreshTokenRepository.findByUserIdAndDeviceId(user.getId(), deviceId)
                    .ifPresentOrElse(
                            existingToken -> existingToken.updateToken(refreshToken, expiresAt, ipAddress, userAgent),
                            () -> {
                                RefreshToken newToken = RefreshToken.create(
                                        user,
                                        deviceId,
                                        deviceName,
                                        refreshToken,
                                        expiresAt,
                                        ipAddress,
                                        userAgent
                                );
                                refreshTokenRepository.save(newToken);
                            }
                    );

            log.info("Refresh Token 저장 완료 - userId: {}, deviceId: {}", userId, deviceId);


            // 래디스에서 임시 코드(1회용) 삭제
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

        // DB에서 리프레시 토큰 삭제
        refreshTokenRepository.deleteByUserIdAndDeviceId(user.getId(), deviceId);
    }

    // 모든 디바이스에서 로그아웃
    @Override
    public void logoutAll(AuthUser authUser) {

        refreshTokenRepository.deleteByUserId(authUser.getUserId());
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

        // 해당 디바이스가 실제로 사용자의 것인지 검증
        RefreshToken token = refreshTokenRepository.findByUserIdAndDeviceId(authUser.getUserId(), deviceId).orElseThrow(
                () -> new AuthException(AuthErrorCode.DEVICE_NOT_FOUND));

        // 삭제
        refreshTokenRepository.delete(token);
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

        // 리프레시 토큰 삭제
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
    }

    // 리프레시 토큰 저장 또는 업데이트
    private void saveOrUpdateRefreshToken(
            User user,
            String deviceId,
            String deviceName,
            String refreshToken,
            HttpServletRequest httpRequest
    ) {
        LocalDateTime newExpiresAt = jwtUtil.calculateRefreshTokenExpiresAt();

        // 클라이언트 IP 추출
        String ipAddress = HttpRequestUtil.getClientIp(httpRequest);

        // User-Agent 추출
        String userAgent = httpRequest.getHeader("User-Agent");

        refreshTokenRepository.findByUserIdAndDeviceId(user.getId(), deviceId)
                .ifPresentOrElse(
                        // 기존 토큰이 있으면 갱신(lastUsedAt도 자동 갱신됨)
                        existingToken -> existingToken.updateToken(refreshToken, newExpiresAt, ipAddress, userAgent),
                        () -> {
                            // 없으면 새로 생성하여 저장(모든 필드 포함)
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
