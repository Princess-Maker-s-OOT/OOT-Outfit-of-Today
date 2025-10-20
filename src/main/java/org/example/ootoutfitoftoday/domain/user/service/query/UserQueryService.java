package org.example.ootoutfitoftoday.domain.user.service.query;

import org.example.ootoutfitoftoday.domain.user.entity.User;

public interface UserQueryService {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    User findByLoginIdAndIsDeletedFalse(String loginId);

    User findByIdAndIsDeletedFalse(Long id);
}
