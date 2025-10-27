package org.example.ootoutfitoftoday.domain.auth.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * RefreshToken 정리 스케줄러
 * - 만료된 토큰을 주기적으로 DB에서 삭제(DB 공간 절약, 보안 강화)
 * - 매일 새벽 3시에 자동 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 만료된 리프레시 토큰 정리(매일 새벽 3시)
     * - expiresAt이 현재 시간보다 이전인 토큰 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("만료된 Refresh Token 정리 작업 시작");

        try {
            refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("만료된 Refresh Token 정리 작업 완료");
        } catch (Exception e) {
            log.error("Refresh Token 정리 작업 중 오류 발생", e);
        }
    }
}
