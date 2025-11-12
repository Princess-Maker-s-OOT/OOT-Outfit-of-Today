package org.example.ootoutfitoftoday.domain.recommendation.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.recommendation.entity.RecommendationBatchHistory;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationErrorCode;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationException;
import org.example.ootoutfitoftoday.domain.recommendation.repository.RecommendationBatchHistoryRepository;
import org.example.ootoutfitoftoday.domain.recommendation.status.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationBatchHistoryServiceImpl implements RecommendationBatchHistoryService {

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
    @Transactional(readOnly = true)
    public Page<RecommendationBatchHistory> getRecentBatchHistory(Pageable pageable) {

        return batchHistoryRepository.findAllByOrderByStartTimeDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationBatchHistory> getBatchHistoryByPeriod(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {

        return batchHistoryRepository.findByStartTimeBetween(startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationBatchHistory> getBatchHistoryByStatus(BatchStatus status) {

        return batchHistoryRepository.findByStatusOrderByStartTimeDesc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationBatchHistory getLastBatchHistory() {

        return batchHistoryRepository.findFirstByOrderByStartTimeDesc()
                .orElseThrow(() -> new RecommendationException(RecommendationErrorCode.BATCH_HISTORY_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationBatchHistory getLastSuccessBatchHistory() {

        return batchHistoryRepository.findFirstByStatusOrderByStartTimeDesc(BatchStatus.SUCCESS)
                .orElseThrow(() -> new RecommendationException(RecommendationErrorCode.BATCH_HISTORY_NOT_FOUND));
    }
}