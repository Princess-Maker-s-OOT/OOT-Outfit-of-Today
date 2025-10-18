package org.example.ootoutfitoftoday.domain.auth.service.command;

import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;

public interface AuthCommandService {

    void signup(AuthSignupRequest request);
}
