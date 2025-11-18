package org.example.ootoutfitoftoday.domain.user.service.command;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userExistsCache", key = "'loginId:' + #user.loginId"),
            @CacheEvict(value = "userExistsCache", key = "'email:' + #user.email"),
            @CacheEvict(value = "userExistsCache", key = "'nickname:' + #user.nickname"),
            @CacheEvict(value = "userExistsCache", key = "'phoneNumber:' + #user.phoneNumber")
    })
    public void save(User user) {
        String roleString = user.getRole().name();

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

        user.linkSocialAccount(socialProvider, socialId, imageUrl);

        return userRepository.save(user);
    }

    @Override
    public String generateUniqueNickname(String baseName) {
        String nickname = baseName;
        int suffix = 1;

        while (userRepository.existsByNickname(nickname)) {
            nickname = baseName + suffix;
            suffix++;
        }

        return nickname;
    }

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
            log.warn("이미 탈퇴한 사용자 - userId: {}", user.getId());
            throw new AuthException(AuthErrorCode.USER_ALREADY_WITHDRAWN);
        }

        LocalDateTime now = LocalDateTime.now();

        userRepository.bulkSoftDeleteUserRelatedData(user.getId(), now);

        user.softDelete();

        userRepository.save(user);
    }

    @Override
    public UserUpdateInfoResponse updateInfo(UserUpdateInfoRequest request, AuthUser authUser) {
        User user = userQueryService.findByIdAndIsDeletedFalse(authUser.getUserId());

        String oldEmail = user.getEmail();
        String oldNickname = user.getNickname();
        String oldPhoneNumber = user.getPhoneNumber();

        if (request.getEmail() != null) {
            if (userQueryService.existsByEmail(request.getEmail()) &&
                    !Objects.equals(user.getEmail(), request.getEmail())) {
                log.warn("이메일 중복 - userId: {}, duplicateEmail: {}", authUser.getUserId(), request.getEmail());
                throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
            }
            user.updateEmail(request.getEmail());
        }

        if (request.getNickname() != null) {
            if (userQueryService.existsByNickname(request.getNickname()) &&
                    !Objects.equals(user.getNickname(), request.getNickname())) {
                log.warn("닉네임 중복 - userId: {}, duplicateNickname: {}", authUser.getUserId(), request.getNickname());
                throw new AuthException(AuthErrorCode.DUPLICATE_NICKNAME);
            }
            user.updateNickname(request.getNickname());
        }

        if (request.getUsername() != null) {
            user.updateUsername(request.getUsername());
        }

        if (request.getPassword() != null) {
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getPhoneNumber() != null) {
            if (userQueryService.existsByPhoneNumber(request.getPhoneNumber()) &&
                    !Objects.equals(user.getPhoneNumber(), request.getPhoneNumber())) {
                log.warn("전화번호 중복 - userId: {}, duplicatePhoneNumber: {}", authUser.getUserId(), request.getPhoneNumber());
                throw new AuthException(AuthErrorCode.DUPLICATE_PHONE_NUMBER);
            }
            user.updatePhoneNumber(request.getPhoneNumber());
        }

        userRepository.flush();

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

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userCache", key = "'id:' + #userId")
    })
    public UserUpdateProfileImageResponse updateProfileImage(Long userId, Long imageId) {
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Image image = imageQueryService.findByIdAndIsDeletedFalse(imageId);

        if (user.getUserImage() == null) {
            UserImage savedUserImage = userImageCommandService.createAndSave(image);
            user.assignProfileImage(savedUserImage);

        } else {
            try {
                UserImage activeUserImage = userImageQueryService.findByIdAndIsDeletedFalse(user.getUserImage().getId()
                );
                user.changeProfileImage(image);
            } catch (UserImageException e) {
                log.warn("기존 프로필 이미지 소프트 삭제됨, 새로 생성 - userId: {}, userImageId: {}", userId, user.getUserImage().getId());
                UserImage savedUserImage = userImageCommandService.createAndSave(image);
                user.assignProfileImage(savedUserImage);
            }
        }

        userRepository.save(user);

        return UserUpdateProfileImageResponse.of(user.getId(), image.getUrl());
    }

    @CacheEvict(value = "userCache", key = "'id:' + #userId")
    public void deleteProfileImage(Long userId) {
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        if (user.getUserImage() == null) {
            log.warn("프로필 이미지 없음 - userId: {}", userId);
            throw new UserImageException(UserImageErrorCode.PROFILE_IMAGE_NOT_FOUND);
        }

        UserImage userImage = userImageQueryService.findByIdAndIsDeletedFalse(user.getUserImage().getId());

        userImageCommandService.softDeleteUserImage(userImage);

        user.removeProfileImage();

        userRepository.save(user);
    }

    @Override
    public void updateMyTradeLocation(UserUpdateTradeLocationRequest request, Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId).orElseThrow(() -> {
            log.warn("거래 위치 수정 실패 - 사용자 없음 - userId: {}", userId);

            return new UserException(UserErrorCode.USER_NOT_FOUND);
        });

        String tradeLocation = PointFormatAndParse.format(request.tradeLongitude(), request.tradeLatitude());

        user.updateTradeLocation(request.tradeAddress(), tradeLocation);

        userRepository.updateTradeLocationAsNativeQuery(userId, user.getTradeAddress(), user.getTradeLocation());
    }

    private void evictUserCaches(
            User user,
            String oldEmail,
            String oldNickname,
            String oldPhoneNumber
    ) {
        evictCache("userCache", "id:" + user.getId());
        evictCache("userCache", "loginId:" + user.getLoginId());

        if (!Objects.equals(oldEmail, user.getEmail())) {
            evictEmailCaches(oldEmail, user.getEmail());
        } else {
            evictCache("userCache", "email:" + user.getEmail());
        }

        if (!Objects.equals(oldNickname, user.getNickname())) {
            evictNicknameCaches(oldNickname, user.getNickname());
        }

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