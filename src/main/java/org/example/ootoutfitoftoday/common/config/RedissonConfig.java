package org.example.ootoutfitoftoday.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

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

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    /**
     * RedissonClient 빈 생성
     * - 분산 락을 사용하려면 이 빈을 주입받아 사용
     */
    @Bean
    public RedissonClient redissonClient() {

        Config config = new Config();

        // 단일 서버 모드 설정
        var singleServerConfig = config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
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
                .setRetryInterval(1500);

        // 비밀번호가 있는 경우에만 설정(CI 환경 대응)
        if (StringUtils.hasText(password)) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(config);
    }
}