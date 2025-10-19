package org.example.ootoutfitoftoday.domain.auth.init;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.auth.service.command.AuthCommandService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AdminDataInitializer {

    private static final String ADMIN_LOGIN_ID = "admin";
    private static final String ADMIN_EMAIL = "admin@oot.com";
    private static final String ADMIN_NICKNAME = "admin";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin00!";
    private static final String ADMIN_PHONE_NUMBER = "010-0000-0000";

    private final AuthCommandService authCommandService;

    @Bean
    public CommandLineRunner initAdmin() {
        
        return args -> authCommandService.initializeAdmin(
                ADMIN_LOGIN_ID,
                ADMIN_EMAIL,
                ADMIN_NICKNAME,
                ADMIN_USERNAME,
                ADMIN_PASSWORD,
                ADMIN_PHONE_NUMBER
        );
    }
}
