package org.example.ootoutfitoftoday.domain.recommendation.service.batch.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.recommendation.entity.RecommendationBatchHistory;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationErrorCode;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationException;
import org.example.ootoutfitoftoday.domain.recommendation.repository.RecommendationBatchHistoryRepository;
import org.example.ootoutfitoftoday.domain.recommendation.status.BatchStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationBatchHistoryCommandServiceImpl implements RecommendationBatchHistoryCommandService {

    private final RecommendationBatchHistoryRepository batchHistoryRepository;
    private final Clock clock;

    @Override
    public RecommendationBatchHistory startBatch() {

        LocalDateTime now = LocalDateTime.now(clock);
        RecommendationBatchHistory batchHistory = RecommendationBatchHistory.createInitial(now);
        RecommendationBatchHistory saved = batchHistoryRepository.save(batchHistory);
        log.info("Batch started - batchHistoryId: {}, startTime: {}", saved.getId(), now);

        return saved;
    }

    @Override
    public void completeBatchSuccess(

            Long batchHistoryId,
            Integer totalUsers,
            Integer successUsers,
            Integer failedUsers,
            Integer totalRecommendations
    ) {
        RecommendationBatchHistory batchHistory = batchHistoryRepository.findById(batchHistoryId)
                .orElseThrow(() -> new RecommendationException(RecommendationErrorCode.BATCH_HISTORY_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(clock);
        batchHistory.markAsSuccess(now, totalUsers, successUsers, failedUsers, totalRecommendations);

        log.info("Batch completed successfully - batchHistoryId: {}, totalUsers: {}, successUsers: {}, " +
                        "failedUsers: {}, totalRecommendations: {}, executionTime: {}ms",
                batchHistoryId, totalUsers, successUsers, failedUsers, totalRecommendations,
                batchHistory.getExecutionTimeMs());
    }

    @Override
    public void completeBatchFailure(Long batchHistoryId, String errorMessage) {

        RecommendationBatchHistory batchHistory = batchHistoryRepository.findById(batchHistoryId)
                .orElseThrow(() -> new RecommendationException(RecommendationErrorCode.BATCH_HISTORY_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(clock);
        batchHistory.markAsFailed(now, errorMessage);

        log.error("Batch failed - batchHistoryId: {}, error: {}", batchHistoryId, errorMessage);
    }

    @Override
    public void handleStaleBatches() {

        LocalDateTime oneHourAgo = LocalDateTime.now(clock).minusHours(1);

        List<RecommendationBatchHistory> staleBatches = batchHistoryRepository.findByStatusOrderByStartTimeDesc(BatchStatus.RUNNING);

        for (RecommendationBatchHistory batch : staleBatches) {
            if (batch.getStartTime().isBefore(oneHourAgo)) {
                batch.markAsFailed(
                        LocalDateTime.now(clock),
                        "Batch was terminated abnormally (stale RUNNING state detected)"
                );
                log.warn("Stale batch detected and marked as FAILED - batchHistoryId: {}, startTime: {}",
                        batch.getId(), batch.getStartTime());
            }
        }
    }
}