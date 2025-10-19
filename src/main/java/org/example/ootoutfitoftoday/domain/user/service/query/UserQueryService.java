package org.example.ootoutfitoftoday.domain.user.service.query;

import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.util.Optional;

public interface UserQueryService {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByLoginIdAndIsDeletedFalse(String loginId);

    Optional<User> findByIdAndIsDeletedFalse(Long id);
}
