package org.example.ootoutfitoftoday.domain.recommendation.service.batch.query;

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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationBatchHistoryQueryServiceImpl implements RecommendationBatchHistoryQueryService {

    private final RecommendationBatchHistoryRepository batchHistoryRepository;

    @Override
    public Page<RecommendationBatchHistory> getRecentBatchHistory(Pageable pageable) {

        return batchHistoryRepository.findAllByOrderByStartTimeDesc(pageable);
    }

    @Override
    public List<RecommendationBatchHistory> getBatchHistoryByPeriod(LocalDateTime startTime, LocalDateTime endTime) {

        return batchHistoryRepository.findByStartTimeBetween(startTime, endTime);
    }

    @Override
    public List<RecommendationBatchHistory> getBatchHistoryByStatus(BatchStatus status) {

        return batchHistoryRepository.findByStatusOrderByStartTimeDesc(status);
    }

    @Override
    public RecommendationBatchHistory getLastBatchHistory() {

        return batchHistoryRepository.findFirstByOrderByStartTimeDesc()
                .orElseThrow(() -> new RecommendationException(RecommendationErrorCode.BATCH_HISTORY_NOT_FOUND));
    }

    @Override
    public RecommendationBatchHistory getLastSuccessBatchHistory() {

        return batchHistoryRepository.findFirstByStatusOrderByStartTimeDesc(BatchStatus.SUCCESS)
                .orElseThrow(() -> new RecommendationException(RecommendationErrorCode.BATCH_HISTORY_NOT_FOUND));
    }
}