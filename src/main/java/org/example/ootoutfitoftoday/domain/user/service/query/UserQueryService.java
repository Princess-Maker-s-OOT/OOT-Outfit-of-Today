package org.example.ootoutfitoftoday.domain.user.service.query;

import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserGetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserQueryService {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    User findByLoginIdAndIsDeletedFalse(String loginId);

    User findByIdAndIsDeletedFalse(Long id);

    User findByEmailAndIsDeletedFalse(String email);

    Optional<User> findBySocialProviderAndSocialId(SocialProvider provider, String socialId);

    UserGetMyInfoResponse getMyInfo(Long userId);

    void verifyPassword(UserPasswordVerificationRequest request, AuthUser authUser);

    int countAllUsers();

    int countByIsDeleted(Boolean isDeleted);

    int countUsersRegisteredSince(LocalDateTime start, LocalDateTime end);

    User findByIdAsNativeQuery(Long id);
}
