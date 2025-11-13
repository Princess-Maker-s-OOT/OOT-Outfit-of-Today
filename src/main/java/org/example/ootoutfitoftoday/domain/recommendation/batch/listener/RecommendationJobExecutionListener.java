package org.example.ootoutfitoftoday.domain.recommendation.batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.recommendation.entity.RecommendationBatchHistory;
import org.example.ootoutfitoftoday.domain.recommendation.service.batch.command.RecommendationBatchHistoryCommandService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 추천 배치 Job의 실행 전후 처리를 담당하는 리스너
 * - Job 시작 시: RecommendationBatchHistory 생성 (RUNNING 상태)
 * - Job 종료 시: RecommendationBatchHistory 업데이트 (성공/실패 여부, 메트릭)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationJobExecutionListener implements JobExecutionListener {

    private final RecommendationBatchHistoryCommandService batchHistoryCommandService;

    private Long batchHistoryId;
    private LocalDateTime startTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = LocalDateTime.now();
        log.info("Starting recommendation batch job: {}", jobExecution.getJobInstance().getJobName());

        // RecommendationBatchHistory 생성 (RUNNING 상태)
        RecommendationBatchHistory history = batchHistoryCommandService.startBatch();
        batchHistoryId = history.getId();

        // JobExecutionContext에 저장하여 Step에서 접근 가능하도록 함
        jobExecution.getExecutionContext().putLong("batchHistoryId", batchHistoryId);

        log.info("Created batch history with ID: {}", batchHistoryId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        LocalDateTime endTime = LocalDateTime.now();
        long executionTimeMs = Duration.between(startTime, endTime).toMillis();

        // Step 실행 결과에서 메트릭 수집
        BatchMetrics metrics = collectMetrics(jobExecution);

        log.info("Recommendation batch job completed - Total: {}, Success: {}, Failed: {}, Recommendations: {}, Duration: {}ms",
                metrics.totalUsers, metrics.successUsers, metrics.failedUsers,
                metrics.totalRecommendations, executionTimeMs);

        // Job 성공 여부 판단
        boolean isSuccess = jobExecution.getExitStatus().getExitCode().equals("COMPLETED");

        if (isSuccess) {
            batchHistoryCommandService.completeBatchSuccess(
                    batchHistoryId,
                    endTime,
                    metrics.totalUsers,
                    metrics.successUsers,
                    metrics.failedUsers,
                    metrics.totalRecommendations,
                    executionTimeMs
            );
        } else {
            String errorMessage = jobExecution.getAllFailureExceptions().stream()
                    .map(Throwable::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Unknown error");

            batchHistoryCommandService.completeBatchFailure(
                    batchHistoryId,
                    endTime,
                    executionTimeMs,
                    errorMessage
            );
        }
    }

    /**
     * Step 실행 결과로부터 메트릭을 수집합니다.
     */
    private BatchMetrics collectMetrics(JobExecution jobExecution) {
        BatchMetrics metrics = new BatchMetrics();

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            // Read Count = 총 처리된 사용자 수
            metrics.totalUsers += (int) stepExecution.getReadCount();

            /**
             * Write Count = 성공한 사용자 수 (실패한 경우 write되지 않음)
             * 실제로는 Processor에서 RecommendationBatchResult로 성공/실패를 구분하므로
             *  ExecutionContext에서 커스텀 메트릭을 읽어야 함
             */

            // ExecutionContext에서 커스텀 메트릭 읽기
            if (stepExecution.getExecutionContext().containsKey("successUsers")) {
                metrics.successUsers += stepExecution.getExecutionContext().getInt("successUsers");
            }

            if (stepExecution.getExecutionContext().containsKey("failedUsers")) {
                metrics.failedUsers += stepExecution.getExecutionContext().getInt("failedUsers");
            }

            if (stepExecution.getExecutionContext().containsKey("totalRecommendations")) {
                metrics.totalRecommendations += stepExecution.getExecutionContext().getInt("totalRecommendations");
            }

            // ExecutionContext에 메트릭이 없는 경우 기본값 사용
            if (metrics.successUsers == 0 && metrics.failedUsers == 0) {
                metrics.successUsers = (int) stepExecution.getWriteCount();
                metrics.totalUsers = (int) stepExecution.getReadCount();
            }
        }

        return metrics;
    }

    private static class BatchMetrics {
        int totalUsers = 0;
        int successUsers = 0;
        int failedUsers = 0;
        int totalRecommendations = 0;
    }
}