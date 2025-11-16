package org.example.ootoutfitoftoday.domain.auth.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.repository.RedisRefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis 기반 RefreshToken 정리 스케줄러
 * - Sorted Set의 score(만료시간)를 기준으로 만료된 토큰 자동 삭제
 * - 매일 새벽 3시에 자동 실행
 * - Redis 메모리 최적화 및 보안 강화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RedisRefreshTokenRepository redisRefreshTokenRepository;

    /**
     * 만료된 리프레시 토큰 정리(매일 새벽 3시)
     * - Sorted Set의 score가 현재 시간보다 이전인 토큰 삭제
     * - 역인덱스 및 메타데이터도 함께 정리
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        log.info("=== Redis 만료 토큰 정리 작업 시작 ===");

        try {
            // Redis에서 만료된 토큰 삭제
            long deletedCount = redisRefreshTokenRepository.deleteExpiredTokens();

            log.info("=== Redis 만료 토큰 정리 작업 완료 - 삭제된 토큰 수: {} ===", deletedCount);
        } catch (Exception e) {
            log.error("Redis 토큰 정리 작업 중 오류 발생", e);
        }
    }
}