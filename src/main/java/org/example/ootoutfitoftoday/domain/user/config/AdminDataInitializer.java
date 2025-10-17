package org.example.ootoutfitoftoday.domain.user.config;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.user.service.UserService;
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

    private final UserService userService;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> userService.initializeAdmin(
                ADMIN_LOGIN_ID,
                ADMIN_EMAIL,
                ADMIN_NICKNAME,
                ADMIN_USERNAME,
                ADMIN_PASSWORD,
                ADMIN_PHONE_NUMBER
        );
    }
}
