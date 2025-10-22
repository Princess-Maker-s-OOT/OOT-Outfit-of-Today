package org.example.ootoutfitoftoday.domain.auth.service.command;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthLoginRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthWithdrawRequest;
import org.example.ootoutfitoftoday.domain.auth.dto.response.AuthLoginResponse;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.chat.service.command.ChatReferenceToChatroomCommandService;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query.ChatParticipatingUserQueryService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.example.ootoutfitoftoday.domain.user.service.command.UserCommandService;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.example.ootoutfitoftoday.security.jwt.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

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

    /**
     * TODO: 리팩토링 고려
     **/
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
                .build();

        userCommandService.save(user);
    }

    // 로그인
    @Override
    public AuthLoginResponse login(AuthLoginRequest request) {

        User user = userQueryService.findByLoginIdAndIsDeletedFalse(request.getLoginId());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        String token = jwtUtil.createToken(user.getId(), user.getRole());

        return new AuthLoginResponse(token);
    }

    // 회원탈퇴
    @Override
    public void withdraw(AuthWithdrawRequest request, AuthUser authUser) {

        User user = userQueryService.findByIdAndIsDeletedFalse(authUser.getUserId());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }

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
}
