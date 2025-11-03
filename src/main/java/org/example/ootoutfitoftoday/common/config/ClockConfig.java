package org.example.ootoutfitoftoday.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    /**
     * Clock 빈을 정의
     * 시스템 기본 시간대(systemDefaultZone)를 사용하는 Clock을 생성하여 Spring IoC 컨테이너에 등록
     * Service, Scheduler 등에서 LocalDate.now(clock) 형태로 주입받아 사용하며,
     * 테스트 시에는 Mock Clock으로 대체하여 시간 의존성을 제거하고 안정적인 테스트를 가능하게 함
     *
     * @return 시스템 기본 Clock 객체
     */
    @Bean
    public Clock clock() {

        return Clock.systemDefaultZone();
    }
}