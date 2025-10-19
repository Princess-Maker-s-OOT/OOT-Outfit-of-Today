package org.example.ootoutfitoftoday.domain.auth.service.command;

import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;

public interface AuthCommandService {

    void initializeAdmin(
            String loginId,
            String email,
            String nickname,
            String username,
            String password,
            String phoneNumber
    );

    void signup(AuthSignupRequest request);
}
