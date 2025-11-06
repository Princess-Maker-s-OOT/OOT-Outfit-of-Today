package org.example.ootoutfitoftoday.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 설정 클래스
 * - 분산 락(Distributed Lock) 구현용
 * - 동시성 제어가 필요한 경우 사용
 * <p>
 * 사용 예시:
 * - 재고 차감 (동시에 여러 주문 시)
 * - 좋아요 중복 방지
 * - 포인트 적립/차감
 */
@Configuration
public class RedissonConfig {

    @Value("${REDIS_HOST:}")
    private String host;

    @Value("${REDIS_PORT:}")
    private int port;

    @Value("${REDIS_PASSWORD:}")
    private String password;

    /**
     * RedissonClient 빈 생성
     * - 분산 락을 사용하려면 이 빈을 주입받아 사용
     */
    @Bean
    public RedissonClient redissonClient() {

        Config config = new Config();

        // 단일 서버 모드 설정
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password)
                // 연결 풀 크기
                .setConnectionPoolSize(64)
                // 최소 유휴 연결 수
                .setConnectionMinimumIdleSize(10)
                // 유휴 연결 타임아웃 (10초)
                .setIdleConnectionTimeout(10000)
                // 연결 타임아웃 (3초)
                .setConnectTimeout(3000)
                // 명령 타임아웃 (3초)
                .setTimeout(3000)
                // 재시도 횟수
                .setRetryAttempts(3)
                // 재시도 간격 (1.5초)
                // .setRetryInterval(Duration.ofMillis(1500))는 실제 3.52.0 스프링 부트 스타터 패키지에 아직 적용되지 않음
                // 따라서, 현재로서 그대로 쓰는 수 밖에 없음
                .setRetryInterval(1500);

        return Redisson.create(config);
    }
}
