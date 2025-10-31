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

    // 소셜 로그인 사용자 생성
    User createSocialUser(
            String email,
            String nickname,
            String username,
            String imageUrl,
            SocialProvider provider,
            String socialId
    );

    // 일반 계정에 소셜 정보를 연동하는 메서드 추가
    User linkSocialAccount(
            User user,
            SocialProvider socialProvider,
            String socialId,
            String imageUrl);

    // 닉네임 중복 체크 및 고유 닉네임 생성
    String generateUniqueNickname(String baseName);

    UserUpdateInfoResponse updateInfo(UserUpdateInfoRequest request, AuthUser authUser);

    UserUpdateProfileImageResponse updateProfileImage(Long userId, Long imageId);

    void updateMyTradeLocation(UserUpdateTradeLocationRequest request, Long userId);
}
