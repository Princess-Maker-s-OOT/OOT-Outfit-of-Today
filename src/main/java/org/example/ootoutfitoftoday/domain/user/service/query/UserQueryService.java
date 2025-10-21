package org.example.ootoutfitoftoday.domain.user.service.query;

import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.GetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;

public interface UserQueryService {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    User findByLoginIdAndIsDeletedFalse(String loginId);

    User findByIdAndIsDeletedFalse(Long id);

    GetMyInfoResponse getMyInfo(Long userId);

    void verifyPassword(UserPasswordVerificationRequest request, AuthUser authUser);
}
