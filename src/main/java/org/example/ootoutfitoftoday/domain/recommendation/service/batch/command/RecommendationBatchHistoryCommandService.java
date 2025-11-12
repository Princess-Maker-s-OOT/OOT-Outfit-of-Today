package org.example.ootoutfitoftoday.domain.recommendation.service.batch.command;

import org.example.ootoutfitoftoday.domain.recommendation.entity.RecommendationBatchHistory;

public interface RecommendationBatchHistoryCommandService {

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

    // Stale 상태의 배치 처리 (1시간 이상 RUNNING 상태인 경우 FAILED로 처리)
    void handleStaleBatches();
}