package org.example.ootoutfitoftoday.domain.recommendation.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.recommendation.service.batch.command.RecommendationBatchHistoryCommandService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 추천 생성 배치 스케줄러
 * Spring Batch Job을 스케줄링하여 실행
 * 매일 새벽 2시에 자동 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

    private final JobLauncher jobLauncher;
    private final Job recommendationJob;
    private final RecommendationBatchHistoryCommandService batchHistoryCommandService;

    /**
     * 매일 새벽 2시에 추천 배치 실행
     * cron 표현식: 초 분 시 일 월 요일
     * 0 0 2 * * * = 매일 2시 0분 0초
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void generateDailyRecommendations() {

        log.info("=== Starting scheduled recommendation batch job ===");

        // Stale 상태 배치 처리 (1시간 이상 RUNNING 상태인 경우 FAILED로 처리)
        batchHistoryCommandService.handleStaleBatches();

        try {
            // Job 파라미터 생성 (매 실행마다 고유한 파라미터 필요)
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDateTime("executionTime", LocalDateTime.now())
                    .toJobParameters();

            // Spring Batch Job 실행
            jobLauncher.run(recommendationJob, jobParameters);

            log.info("=== Recommendation batch job completed successfully ===");

        } catch (Exception e) {
            log.error("Failed to execute recommendation batch job", e);
            throw new RuntimeException("Recommendation batch job failed", e);
        }
    }
}
