package org.example.ootoutfitoftoday.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean(name = "tossRestTemplate")
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 연결 타임아웃 (ms)
        factory.setReadTimeout(5000);    // 읽기 타임아웃 (ms)

        return new RestTemplate(factory);
    }
}
