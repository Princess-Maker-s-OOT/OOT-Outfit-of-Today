package org.example.ootoutfitoftoday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
        "org.example.ootoutfitoftoday",   // 기존 엔티티
        "com.ootcommon"                   // 공통 모듈 엔티티 (BatchHistory 포함)
})
@EnableJpaRepositories(basePackages = {
        "org.example.ootoutfitoftoday"    // 메인 서버 Repository
})
public class OotOutfitOfTodayApplication {

    public static void main(String[] args) {
        SpringApplication.run(OotOutfitOfTodayApplication.class, args);
    }
}