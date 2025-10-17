package org.example.ootoutfitoftoday.domain.user.service;

public interface UserService {

    boolean existsByLoginId(String loginId);

    void initializeAdmin(
            String loginId,
            String email,
            String nickname,
            String username,
            String password,
            String phoneNumber
    );
}
