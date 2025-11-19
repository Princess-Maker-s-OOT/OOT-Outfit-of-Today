package org.example.ootoutfitoftoday.domain.user.service.command;

import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateInfoRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateTradeLocationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserUpdateInfoResponse;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserUpdateProfileImageResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;

public interface UserCommandService {

    void save(User user);

    void softDeleteUser(User user);

    User createSocialUser(
            String email,
            String nickname,
            String username,
            String imageUrl,
            SocialProvider provider,
            String socialId
    );

    User linkSocialAccount(
            User user,
            SocialProvider socialProvider,
            String socialId,
            String imageUrl);

    String generateUniqueNickname(String baseName);

    UserUpdateInfoResponse updateInfo(UserUpdateInfoRequest request, AuthUser authUser);

    UserUpdateProfileImageResponse updateProfileImage(Long userId, Long imageId);

    void deleteProfileImage(Long userId);

    void updateMyTradeLocation(UserUpdateTradeLocationRequest request, Long userId);
}