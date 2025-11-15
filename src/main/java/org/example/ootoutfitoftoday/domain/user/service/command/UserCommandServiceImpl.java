package org.example.ootoutfitoftoday.domain.user.service.command;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.util.DefaultLocationConstants;
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
import org.example.ootoutfitoftoday.domain.userimage.exception.UserImageErrorCode;
import org.example.ootoutfitoftoday.domain.userimage.exception.UserImageException;
import org.example.ootoutfitoftoday.domain.userimage.service.command.UserImageCommandService;
import org.example.ootoutfitoftoday.domain.userimage.service.query.UserImageQueryService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
    private final UserImageQueryService userImageQueryService;
    private final CacheManager cacheManager;

    // 회원가입 시 캐시 무효화
    // 새로운 사용자가 생성되므로 중복 체크 캐시 무효화 필요
    // loginId, email, nickname, phoneNumber 모두 무효화
    @Override
    @Caching(evict = {
            @CacheEvict(value = "userExistsCache", key = "'loginId:' + #user.loginId"),
            @CacheEvict(value = "userExistsCache", key = "'email:' + #user.email"),
            @CacheEvict(value = "userExistsCache", key = "'nickname:' + #user.nickname"),
            @CacheEvict(value = "userExistsCache", key = "'phoneNumber:' + #user.phoneNumber")
    })
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

    // 소셜 회원 생성 시 캐시 무효화
    @Override
    @Caching(evict = {
            @CacheEvict(value = "userExistsCache", key = "'email:' + #email"),
            @CacheEvict(value = "userExistsCache", key = "'nickname:' + #nickname")
    })
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

        User updateLocationUser = userRepository.save(socialUser);

        updateLocationUser.updateTradeLocation(DefaultLocationConstants.DEFAULT_TRADE_ADDRESS, DefaultLocationConstants.DEFAULT_TRADE_LOCATION);

        userRepository.updateTradeLocationAsNativeQuery(updateLocationUser.getId(), updateLocationUser.getTradeAddress(), updateLocationUser.getTradeLocation());

        return socialUser;
    }

    // 일반 계정에 소셜 정보를 연동하고 DB에 저장 시 캐시 무효화
    // 사용자 정보가 변경되므로 해당 사용자의 모든 캐시 무효화
    @Override
    @Caching(evict = {
            @CacheEvict(value = "userCache", key = "'id:' + #user.id"),
            @CacheEvict(value = "userCache", key = "'loginId:' + #user.loginId"),
            @CacheEvict(value = "userCache", key = "'email:' + #user.email")
    })
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

    // 회원탈퇴 시 캐시 무효화
    // 탈퇴한 사용자의 모든 캐시 정보 삭제
    @Override
    @Caching(evict = {
            @CacheEvict(value = "userCache", key = "'id:' + #user.id"),
            @CacheEvict(value = "userCache", key = "'loginId:' + #user.loginId"),
            @CacheEvict(value = "userCache", key = "'email:' + #user.email"),
            @CacheEvict(value = "userExistsCache", key = "'loginId:' + #user.loginId"),
            @CacheEvict(value = "userExistsCache", key = "'email:' + #user.email"),
            @CacheEvict(value = "userExistsCache", key = "'nickname:' + #user.nickname"),
            @CacheEvict(value = "userExistsCache", key = "'phoneNumber:' + #user.phoneNumber")
    })
    public void softDeleteUser(User user) {

        if (user.isDeleted()) {
            throw new AuthException(AuthErrorCode.USER_ALREADY_WITHDRAWN);
        }

        LocalDateTime now = LocalDateTime.now();

        userRepository.bulkSoftDeleteUserRelatedData(user.getId(), now);

        user.softDelete();

        userRepository.save(user);
    }

    // 회원정보 수정 시 캐시 무효화
    // 변경된 정보에 대한 캐시만 선택적으로 무효화
    // 이메일, 닉네임, 전화번호가 변경되면 해당 exists 캐시도 무효화
    @Override
    public UserUpdateInfoResponse updateInfo(UserUpdateInfoRequest request, AuthUser authUser) {

        User user = userQueryService.findByIdAndIsDeletedFalse(authUser.getUserId());

        // 변경 전 정보 저장(캐시 무효화용)
        String oldEmail = user.getEmail();
        String oldNickname = user.getNickname();
        String oldPhoneNumber = user.getPhoneNumber();

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

        // 변경된 정보에 대한 캐시 무효화
        evictUserCaches(user, oldEmail, oldNickname, oldPhoneNumber);

        entityManager.clear();

        user = userRepository.findByIdAsNativeQuery(authUser.getUserId());

        return UserUpdateInfoResponse.of(
                user.getEmail(),
                user.getNickname(),
                user.getUsername(),
                user.getPhoneNumber()
        );
    }

    // 프로필 이미지 수정(등록) 시 캐시 무효화
    @Override
    @Caching(evict = {
            @CacheEvict(value = "userCache", key = "'id:' + #userId")
    })
    public UserUpdateProfileImageResponse updateProfileImage(Long userId, Long imageId) {

        // 사용자 조회
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        // 이미지 조회
        Image image = imageQueryService.findByIdAndIsDeletedFalse(imageId);

        // 프로필 이미지 업데이트
        if (user.getUserImage() == null) {
            // 기존 프로필 이미지가 없음 -> 새로 생성하고 저장
            UserImage savedUserImage = userImageCommandService.createAndSave(image);
            user.assignProfileImage(savedUserImage);

        } else {
            // 기존 프로필 이미지가 있음 -> 활성 상태 확인 후 처리
            try {
                UserImage activeUserImage = userImageQueryService.findByIdAndIsDeletedFalse(user.getUserImage().getId()
                );
                // 활성 상태인 경우 -> 기존 이미지 교체
                user.changeProfileImage(image);
            } catch (UserImageException e) {
                // 기존 프로필 이미지가 소프트 딜리트 되어 조회 실패 시 새로 생성
                UserImage savedUserImage = userImageCommandService.createAndSave(image);
                user.assignProfileImage(savedUserImage);
            }
        }

        userRepository.save(user);

        return UserUpdateProfileImageResponse.of(user.getId(), image.getUrl());
    }

    // 프로필 이미지 삭제(소프트 딜리트) 시 캐시 무효화
    @CacheEvict(value = "userCache", key = "'id:' + #userId")
    public void deleteProfileImage(Long userId) {

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        // UserImage 존재 여부 체크
        if (user.getUserImage() == null) {
            throw new UserImageException(UserImageErrorCode.PROFILE_IMAGE_NOT_FOUND);
        }

        // DB에서 실제 UserImage 조회(소프트 삭제 확인)
        UserImage userImage = userImageQueryService.findByIdAndIsDeletedFalse(user.getUserImage().getId());

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

    // 캐시 무효화 헬퍼 메서드
    // - 프로그래밍 방식으로 캐시 무효화(변경된 항목만 선택적으로)
    private void evictUserCaches(
            User user,
            String oldEmail,
            String oldNickname,
            String oldPhoneNumber
    ) {
        // 기본 사용자 정보 캐시 무효화
        evictCache("userCache", "id:" + user.getId());
        evictCache("userCache", "loginId:" + user.getLoginId());

        // 이메일이 변경된 경우
        if (!Objects.equals(oldEmail, user.getEmail())) {
            evictEmailCaches(oldEmail, user.getEmail());
        } else {
            evictCache("userCache", "email:" + user.getEmail());
        }

        // 닉네임이 변경된 경우
        if (!Objects.equals(oldNickname, user.getNickname())) {
            evictNicknameCaches(oldNickname, user.getNickname());
        }

        // 전화번호가 변경된 경우
        if (!Objects.equals(oldPhoneNumber, user.getPhoneNumber())) {
            evictPhoneNumberCaches(oldPhoneNumber, user.getPhoneNumber());
        }
    }

    private void evictEmailCaches(String oldEmail, String newEmail) {

        evictCache("userCache", "email:" + oldEmail);
        evictCache("userCache", "email:" + newEmail);
        evictCache("userExistsCache", "email:" + oldEmail);
        evictCache("userExistsCache", "email:" + newEmail);
    }

    private void evictNicknameCaches(String oldNickname, String newNickname) {

        evictCache("userExistsCache", "nickname:" + oldNickname);
        evictCache("userExistsCache", "nickname:" + newNickname);
    }

    private void evictPhoneNumberCaches(String oldPhoneNumber, String newPhoneNumber) {

        evictCache("userExistsCache", "phoneNumber:" + oldPhoneNumber);
        evictCache("userExistsCache", "phoneNumber:" + newPhoneNumber);
    }

    private void evictCache(String cacheName, String key) {

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
