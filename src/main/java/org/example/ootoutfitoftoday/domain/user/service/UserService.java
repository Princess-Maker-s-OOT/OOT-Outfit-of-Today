package org.example.ootoutfitoftoday.domain.user.service;

import org.example.ootoutfitoftoday.domain.user.entity.User;

public interface UserService {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    void save(User user);

    void initializeAdmin(
            String loginId,
            String email,
            String nickname,
            String username,
            String password,
            String phoneNumber
    );
}
