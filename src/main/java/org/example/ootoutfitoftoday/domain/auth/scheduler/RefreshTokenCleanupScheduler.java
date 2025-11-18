package org.example.ootoutfitoftoday.domain.auth.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

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