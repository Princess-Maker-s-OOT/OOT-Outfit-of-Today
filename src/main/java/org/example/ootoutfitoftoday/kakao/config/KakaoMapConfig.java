package org.example.ootoutfitoftoday.kakao.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KakaoMapConfig {

    private static final int CONNECT_TIMEOUT = 5000; // 5초
    private static final int READ_TIMEOUT = 5000; // 5초

    @Bean(name = "kakaoMapRestTemplate")
    public RestTemplate kakaoMapRestTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);

        return builder
                .requestFactory(() -> factory)
                .build();
    }
}