package org.example.ootoutfitoftoday.domain.recommendation.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.recommendation.batch.dto.RecommendationBatchResult;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Step 실행 메트릭을 추적하는 리스너
 * Chunk 처리 결과를 집계하여 ExecutionContext에 저장
 */
@Slf4j
@Component
public class RecommendationStepExecutionListener implements StepExecutionListener {

    private final AtomicInteger successUsers = new AtomicInteger(0);
    private final AtomicInteger failedUsers = new AtomicInteger(0);
    private final AtomicInteger totalRecommendations = new AtomicInteger(0);

    @Override
    public void beforeStep(StepExecution stepExecution) {

        log.info("Starting step: {}", stepExecution.getStepName());
        successUsers.set(0);
        failedUsers.set(0);
        totalRecommendations.set(0);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // 최종 메트릭을 ExecutionContext에 저장
        stepExecution.getExecutionContext().putInt("successUsers", successUsers.get());
        stepExecution.getExecutionContext().putInt("failedUsers", failedUsers.get());
        stepExecution.getExecutionContext().putInt("totalRecommendations", totalRecommendations.get());

        log.info("Step completed - Success: {}, Failed: {}, Total Recommendations: {}",
                successUsers.get(), failedUsers.get(), totalRecommendations.get());

        return stepExecution.getExitStatus();
    }

    /**
     * Chunk 처리 후 호출되어 메트릭을 업데이트
     * Writer에서 호출됨
     */
    public void afterChunkWrite(Chunk<? extends RecommendationBatchResult> chunk) {

        chunk.getItems().forEach(result -> {
            if (result.success()) {
                successUsers.incrementAndGet();
                totalRecommendations.addAndGet(result.getRecommendationCount());
            } else {
                failedUsers.incrementAndGet();
            }
        });
    }
}