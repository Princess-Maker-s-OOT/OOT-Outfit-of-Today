package org.example.ootoutfitoftoday.domain.user.service.command;

import org.example.ootoutfitoftoday.domain.user.entity.User;

public interface UserCommandService {

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
