package org.example.ootoutfitoftoday.domain.auth.service;

import org.example.ootoutfitoftoday.domain.auth.dto.request.AuthSignupRequest;

public interface AuthService {

    void signup(AuthSignupRequest request);
}
