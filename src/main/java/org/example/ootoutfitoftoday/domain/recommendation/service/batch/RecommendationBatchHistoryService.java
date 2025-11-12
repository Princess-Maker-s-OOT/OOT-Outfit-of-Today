package org.example.ootoutfitoftoday.domain.recommendation.service.batch;

import org.example.ootoutfitoftoday.domain.recommendation.entity.RecommendationBatchHistory;
import org.example.ootoutfitoftoday.domain.recommendation.status.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface RecommendationBatchHistoryService {

    // 배치 이력 시작 기록
    RecommendationBatchHistory startBatch();

    // 배치 성공 기록
    void completeBatchSuccess(
            Long batchHistoryId,
            Integer totalUsers,
            Integer successUsers,
            Integer failedUsers,
            Integer totalRecommendations
    );

    // 배치 실패 기록
    void completeBatchFailure(Long batchHistoryId, String errorMessage);

    // 최근 배치 이력 조회
    Page<RecommendationBatchHistory> getRecentBatchHistory(Pageable pageable);

    // 특정 기간의 배치 이력 조회
    List<RecommendationBatchHistory> getBatchHistoryByPeriod(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // 특정 상태의 배치 이력 조회
    List<RecommendationBatchHistory> getBatchHistoryByStatus(BatchStatus status);

    // 가장 최근 배치 이력 조회
    RecommendationBatchHistory getLastBatchHistory();

    // 가장 최근 성공한 배치 이력 조회
    RecommendationBatchHistory getLastSuccessBatchHistory();
}