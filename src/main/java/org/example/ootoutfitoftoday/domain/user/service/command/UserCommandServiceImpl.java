package org.example.ootoutfitoftoday.domain.user.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateInfoRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.GetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.exception.UserErrorCode;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
import org.example.ootoutfitoftoday.domain.user.repository.UserRepository;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserQueryService userQueryService;

    @Override
    public void save(User user) {

        userRepository.save(user);
    }

    @Override
    public void softDeleteUser(User user) {

        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.USER_ALREADY_WITHDRAWN);
        }

        user.softDelete();
        userRepository.save(user);
    }

    // 회원정보 수정
    @Override
    public GetMyInfoResponse updateMyInfo(UserUpdateInfoRequest request, AuthUser authUser) {

        User user = userQueryService.findByIdAndIsDeletedFalse(authUser.getUserId());

        // 이미지(null 허용)
        if (request.getImageUrl() != null) {
            user.updateImageUrl(request.getImageUrl());
        } else {
        }

        // 이메일
        if (hasTextNoSpace(request.getEmail())) {
            if (userQueryService.existsByEmail(request.getEmail()) &&
                    !Objects.equals(user.getEmail(), request.getEmail())) {
                throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
            }
            user.updateEmail(request.getEmail());
        } else {
        }

        // 닉네임 (띄어쓰기 허용)
        if (hasTextAllowSpace(request.getNickname())) {
            if (userQueryService.existsByNickname(request.getNickname()) &&
                    !Objects.equals(user.getNickname(), request.getNickname())) {
                throw new AuthException(AuthErrorCode.DUPLICATE_NICKNAME);
            }
            user.updateNickname(request.getNickname());
        } else {
        }

        // 이름
        if (hasTextNoSpace(request.getUsername())) {
            user.updateUsername(request.getUsername());
        } else {
        }

        // 비밀번호
        if (hasTextNoSpace(request.getPassword())) {
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
        } else {
        }

        // 전화번호
        if (hasTextNoSpace(request.getPhoneNumber())) {
            if (userQueryService.existsByPhoneNumber(request.getPhoneNumber()) &&
                    !Objects.equals(user.getPhoneNumber(), request.getPhoneNumber())) {
                throw new AuthException(AuthErrorCode.DUPLICATE_PHONE_NUMBER);
            }
            user.updatePhoneNumber(request.getPhoneNumber());
        } else {
        }

        userRepository.save(user);

        return GetMyInfoResponse.from(user);
    }

    // 공백 비허용(이메일, 이름, 비밀번호, 전화번호용)
    private boolean hasTextNoSpace(String value) {
        return value != null && !value.isBlank() && !value.contains(" ");
    }

    // 중간 띄어쓰기 허용 & 앞뒤 공백 비허용(닉네임용)
    private boolean hasTextAllowSpace(String value) {
        return value != null && !value.isBlank() && value.trim().equals(value);
    }
}
