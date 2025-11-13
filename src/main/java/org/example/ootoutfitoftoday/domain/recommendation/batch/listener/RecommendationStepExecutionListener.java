package org.example.ootoutfitoftoday.domain.recommendation.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.recommendation.batch.dto.RecommendationBatchResult;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Step 실행 메트릭을 추적하는 리스너
 * <p>
 * ItemWriteListener를 구현하여 Chunk 처리 결과를 자동으로 집계하고
 * ExecutionContext에 저장
 * {@code @StepScope}를 사용하여 각 Step 실행마다 새로운 인스턴스를 생성하고,
 * 인스턴스 변수(메트릭 카운터)가 Step 간에 격리되도록 보장
 * 이를 통해 동시 실행되는 Job들 간의 상태 오염을 방지
 */
@Slf4j
@StepScope
@Component
public class RecommendationStepExecutionListener
        implements StepExecutionListener, ItemWriteListener<RecommendationBatchResult> {

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

    @Override
    public void beforeWrite(Chunk<? extends RecommendationBatchResult> chunk) {
        // Chunk 쓰기 전 처리 (필요시 구현)
    }

    /**
     * Chunk 쓰기 완료 후 자동으로 호출되어 메트릭을 업데이트합니다.
     * Spring Batch가 ItemWriter.write() 호출 후 자동으로 실행합니다.
     */
    @Override
    public void afterWrite(Chunk<? extends RecommendationBatchResult> chunk) {
        chunk.getItems().forEach(result -> {
            if (result.success()) {
                successUsers.incrementAndGet();
                totalRecommendations.addAndGet(result.getRecommendationCount());
            } else {
                failedUsers.incrementAndGet();
            }
        });

        log.debug("Chunk write completed - Current totals: Success={}, Failed={}, Recommendations={}",
                successUsers.get(), failedUsers.get(), totalRecommendations.get());
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends RecommendationBatchResult> chunk) {
        // Chunk 쓰기 실패 시 처리
        log.error("Error writing chunk: {}", exception.getMessage(), exception);

        // 실패한 청크의 모든 아이템을 실패로 카운트
        int chunkSize = chunk.getItems().size();
        failedUsers.addAndGet(chunkSize);

        log.warn("Marked {} users as failed due to write error", chunkSize);
    }
}