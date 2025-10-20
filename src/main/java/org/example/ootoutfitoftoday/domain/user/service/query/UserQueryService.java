package org.example.ootoutfitoftoday.domain.user.service.query;

import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserGetResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;

public interface UserQueryService {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    User findByLoginIdAndIsDeletedFalse(String loginId);

    User findByIdAndIsDeletedFalse(Long id);

    UserGetResponse getMyInfo(Long userId);

    void verifyPassword(UserPasswordVerificationRequest request, AuthUser authUser);
}
