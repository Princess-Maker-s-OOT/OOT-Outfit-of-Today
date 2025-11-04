package org.example.ootoutfitoftoday.domain.auth.service.command;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandServiceImpl implements AuthCommandService {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final ChatParticipatingUserQueryService chatParticipatingUserQueryService;
    private final ChatReferenceToChatroomCommandService chatReferenceToChatroomCommandService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // 사용자당 최대 디바이스 수 설정(application.yml에서 주입)
    @Value("${jwt.max-devices-per-user:5}")
    private int maxDevicesPerUser;

    // 관리자 계정 초기 생성 자동
    @Override
    public void initializeAdmin(
            String loginId,
            String email,
            String nickname,
            String username,
            String password,
            String phoneNumber
    ) {
        if (!userQueryService.existsByLoginId(loginId)) {
            User admin = User.createAdmin(
                    loginId,
                    email,
                    nickname,
                    username,
                    passwordEncoder.encode(password),
                    phoneNumber
            );
            userCommandService.save(admin);
            log.info("관리자 계정 초기 생성 완료되었습니다.");
        } else {
            log.info("관리자 계정이 이미 존재합니다.");
        }
    }

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

        User user = userQueryService.findByLoginIdAndIsDeletedFalse(request.getLoginId());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        // 단일 쿼리로 디바이스 수 확인 및 정리
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserIdOrderByLastUsedAtDesc(user.getId());

        if (tokens.size() >= maxDevicesPerUser) {
            // 가장 오래된 디바이스 삭제
            RefreshToken oldestToken = tokens.get(tokens.size() - 1);
            refreshTokenRepository.delete(oldestToken);
            log.info("최대 디바이스 수 초과로 가장 오래된 디바이스 삭제: userId={}, deviceId={}",
                    user.getId(), oldestToken.getDeviceId());
        }

        // 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole());

        // 리프레시 토큰 생성 및 DB 저장
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        // 유저 정보와 함께 리프레시 토큰 저장
        saveOrUpdateRefreshToken(user, request.getDeviceId(), request.getDeviceName(), refreshToken, httpRequest);

        return new AuthLoginResponse(accessToken, refreshToken);
    }

    // 토큰 재발급(액세스 토큰 만료 시 클라이언트가 저장해둔 리프레시 토큰으로 새 액세스 토큰 발급)
    // 바디로 전달받은 리프레시 토큰을 파라미터로 받음
    @Override
    public AuthLoginResponse refresh(String refreshToken, String deviceId) {

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
        LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpirationMillis() / 1000);

        // updateToken 호출 시 lastUsedAt도 자동 갱신됨
        storedToken.updateToken(newRefreshToken, newExpiresAt);

        return new AuthLoginResponse(newAccessToken, newRefreshToken);
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
    public void removeDevice(AuthUser authUser, String deviceId) {

        refreshTokenRepository.deleteByUserIdAndDeviceId(authUser.getUserId(), deviceId);
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
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpirationMillis() / 1000);

        // 클라이언트 IP 추출
        String ipAddress = HttpRequestUtil.getClientIp(httpRequest);

        // User-Agent 추출
        String userAgent = httpRequest.getHeader("User-Agent");

        refreshTokenRepository.findByUserIdAndDeviceId(user.getId(), deviceId)
                .ifPresentOrElse(
                        // 기존 토큰이 있으면 갱신(lastUsedAt도 자동 갱신됨)
                        existingToken -> existingToken.updateToken(refreshToken, expiresAt),
                        () -> {
                            // 없으면 새로 생성하여 저장(모든 필드 포함)
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
    }
}
