package org.example.ootoutfitoftoday.domain.recommendation.service.batch.query;

import org.example.ootoutfitoftoday.domain.recommendation.entity.RecommendationBatchHistory;
import org.example.ootoutfitoftoday.domain.recommendation.status.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface RecommendationBatchHistoryQueryService {

    // 최근 배치 이력 조회
    Page<RecommendationBatchHistory> getRecentBatchHistory(Pageable pageable);

    // 특정 기간의 배치 이력 조회
    List<RecommendationBatchHistory> getBatchHistoryByPeriod(LocalDateTime startTime, LocalDateTime endTime);

    // 특정 상태의 배치 이력 조회
    List<RecommendationBatchHistory> getBatchHistoryByStatus(BatchStatus status);

    // 가장 최근 배치 이력 조회
    RecommendationBatchHistory getLastBatchHistory();

    // 가장 최근 성공한 배치 이력 조회
    RecommendationBatchHistory getLastSuccessBatchHistory();
}
