package org.example.ootoutfitoftoday.domain.user.service.query;

import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.GetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.time.LocalDateTime;

public interface UserQueryService {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    User findByLoginIdAndIsDeletedFalse(String loginId);

    User findByIdAndIsDeletedFalse(Long id);

    GetMyInfoResponse getMyInfo(Long userId);

    void verifyPassword(UserPasswordVerificationRequest request, AuthUser authUser);

    int countAllUsers();

    int countByIsDeleted(Boolean isDeleted);

    int countUsersRegisteredSince(LocalDateTime start, LocalDateTime end);
}
