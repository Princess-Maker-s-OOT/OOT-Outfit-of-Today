package org.example.ootoutfitoftoday.domain.user.service.command;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.service.query.ImageQueryService;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateInfoRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateTradeLocationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserUpdateInfoResponse;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserUpdateProfileImageResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.exception.UserErrorCode;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
import org.example.ootoutfitoftoday.domain.user.repository.UserRepository;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.example.ootoutfitoftoday.domain.userimage.entity.UserImage;
import org.example.ootoutfitoftoday.domain.userimage.service.command.UserImageCommandService;
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
    private final ImageQueryService imageQueryService;
    private final UserImageCommandService userImageCommandService;

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

    // 소셜 회원 생성
    @Override
    public User createSocialUser(
            String email,
            String nickname,
            String username,
            String imageUrl,
            SocialProvider provider,
            String socialId
    ) {
        User socialUser = User.createFromSocial(
                email,
                nickname,
                username,
                imageUrl,
                provider,
                socialId
        );

        return userRepository.save(socialUser);
    }

    // 일반 계정에 소셜 정보를 연동하고 DB에 저장
    @Override
    public User linkSocialAccount(
            User user,
            SocialProvider socialProvider,
            String socialId,
            String imageUrl) {

        // User 엔티티의 도메인 메서드를 호출하여 정보 업데이트
        user.linkSocialAccount(socialProvider, socialId, imageUrl);

        // 트랜잭션 내에서 변경된 엔티티를 명시적으로 저장
        return userRepository.save(user);
    }

    @Override
    public String generateUniqueNickname(String baseName) {

        String nickname = baseName;
        // 접미사
        int suffix = 1;

        // 닉네임이 DB에 이미 존재하는 경우, 접미사를 붙여 고유한 닉네임 생성
        while (userRepository.existsByNickname(nickname)) {
            nickname = baseName + suffix;
            suffix++;
        }

        return nickname;
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
    //TODO: 리팩토링 고려
    @Override
    public UserUpdateInfoResponse updateInfo(UserUpdateInfoRequest request, AuthUser authUser) {

        User user = userQueryService.findByIdAndIsDeletedFalse(authUser.getUserId());

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

        return UserUpdateInfoResponse.of(
                user.getEmail(),
                user.getNickname(),
                user.getUsername(),
                user.getPhoneNumber()
        );
    }

    // 프로필 이미지 수정(등록)
    @Override
    public UserUpdateProfileImageResponse updateProfileImage(Long userId, Long imageId) {

        // 사용자 조회
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        // 이미지 조회
        Image image = imageQueryService.findByIdAndIsDeletedFalse(imageId);

        // 프로필 이미지 업데이트
        // 기존 이미지가 있으면 이미지만 교체
        // 없으면 새로운 이미지 생성
        user.updateProfileImage(image);

        userRepository.save(user);

        return UserUpdateProfileImageResponse.of(user.getId(), image.getUrl());
    }

    // 프로필 이미지 삭제(소프트 딜리트)
    public void deleteProfileImage(Long userId) {

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        UserImage userImage = user.getUserImage();
        if (userImage == null) {
            throw new UserException(UserErrorCode.PROFILE_IMAGE_NOT_FOUND);
        }

        userImageCommandService.softDeleteUserImage(userImage);

        // User의 UserImage 참조 제거 및 imageUrl 초기화
        user.removeProfileImage();

        userRepository.save(user);
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
    }
}
