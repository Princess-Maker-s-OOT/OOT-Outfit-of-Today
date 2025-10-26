package org.example.ootoutfitoftoday.domain.user.service.command;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateInfoRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateTradeLocationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.GetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.exception.UserErrorCode;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
import org.example.ootoutfitoftoday.domain.user.repository.UserRepository;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserQueryService userQueryService;
    private final EntityManager entityManager;

    @Override
    public void save(User user) {

        String roleString = user.getRole().name();

        // .save 메서드 대신 POINT 타입 컬럼의 값을 정상적으로 넣기 위한 Native Query를 이용하여 작성
        userRepository.saveAsNativeQuery(
                user.getLoginId(),
                user.getEmail(),
                user.getNickname(),
                user.getUsername(),
                user.getPassword(),
                user.getPhoneNumber(),
                roleString,
                user.getTradeAddress(),
                user.getTradeLocation(),
                user.getImageUrl(),
                false
        );
    }

    @Override
    public void softDeleteUser(User user) {

        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.USER_ALREADY_WITHDRAWN);
        }

        LocalDateTime now = LocalDateTime.now();

        userRepository.bulkSoftDeleteUserRelatedData(user.getId(), now);

        user.softDelete();

        userRepository.save(user);
    }

    // 회원정보 수정

    /**
     * TODO: 리팩토링 고려
     **/
    @Override
    public GetMyInfoResponse updateMyInfo(UserUpdateInfoRequest request, AuthUser authUser) {

        User user = userQueryService.findByIdAndIsDeletedFalse(authUser.getUserId());

        // 이미지(null 허용)
        if (request.getImageUrl() != null) {
            user.updateImageUrl(request.getImageUrl());
        }

        // 이메일
        if (request.getEmail() != null) {
            if (userQueryService.existsByEmail(request.getEmail()) &&
                    !Objects.equals(user.getEmail(), request.getEmail())) {
                throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
            }
            user.updateEmail(request.getEmail());
        }

        // 닉네임 (중간 띄어쓰기 허용, 앞뒤 공백 금지는 DTO에서 검증)
        if (request.getNickname() != null) {
            if (userQueryService.existsByNickname(request.getNickname()) &&
                    !Objects.equals(user.getNickname(), request.getNickname())) {
                throw new AuthException(AuthErrorCode.DUPLICATE_NICKNAME);
            }
            user.updateNickname(request.getNickname());
        }

        // 이름
        if (request.getUsername() != null) {
            user.updateUsername(request.getUsername());
        }

        // 비밀번호
        if (request.getPassword() != null) {
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
        }

        // 전화번호
        if (request.getPhoneNumber() != null) {
            if (userQueryService.existsByPhoneNumber(request.getPhoneNumber()) &&
                    !Objects.equals(user.getPhoneNumber(), request.getPhoneNumber())) {
                throw new AuthException(AuthErrorCode.DUPLICATE_PHONE_NUMBER);
            }
            user.updatePhoneNumber(request.getPhoneNumber());
        }

        userRepository.flush();

        entityManager.clear();

        user = userRepository.findByIdAsNativeQuery(authUser.getUserId());

        return GetMyInfoResponse.from(user);
    }

    // 유저 거래 위치 수정
    @Override
    public void updateMyTradeLocation(UserUpdateTradeLocationRequest request, Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND)
        );

        String tradeLocation = PointFormatAndParse.format(request.tradeLongitude(), request.tradeLatitude());

        user.updateTradeLocation(request.tradeAddress(), tradeLocation);

        userRepository.updateTradeLocationAsNativeQuery(userId, user.getTradeAddress(), user.getTradeLocation());

        entityManager.refresh(user);
    }
}
