package org.example.ootoutfitoftoday.kakao.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KakaoMapConfig {

    private static final int CONNECT_TIMEOUT = 5000; // 5초
    private static final int READ_TIMEOUT = 5000; // 5초

//    @Bean
//    public RestTemplate restTemplate(RestTemplateBuilder builder) {
//        return builder
//                .setConnectTimeout(Duration.ofMillis(CONNECT_TIMEOUT))
//                .setReadTimeout(Duration.ofMillis(READ_TIMEOUT))
//                .build();
//    }
}