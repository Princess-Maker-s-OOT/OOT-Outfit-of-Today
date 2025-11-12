package org.example.ootoutfitoftoday.domain.recommendation.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.recommendation.entity.RecommendationBatchHistory;
import org.example.ootoutfitoftoday.domain.recommendation.status.BatchStatus;

import java.time.LocalDateTime;

/**
 * 배치 실행 이력 응답 DTO
 */
@Builder(access = AccessLevel.PRIVATE)
public record RecommendationBatchHistoryResponse(

        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BatchStatus status,
        Integer totalUsers,
        Integer successUsers,
        Integer failedUsers,
        Integer totalRecommendations,
        Long executionTimeMs,
        String errorMessage
) {
    public static RecommendationBatchHistoryResponse from(RecommendationBatchHistory batchHistory) {

        return RecommendationBatchHistoryResponse.builder()
                .id(batchHistory.getId())
                .startTime(batchHistory.getStartTime())
                .endTime(batchHistory.getEndTime())
                .status(batchHistory.getStatus())
                .totalUsers(batchHistory.getTotalUsers())
                .successUsers(batchHistory.getSuccessUsers())
                .failedUsers(batchHistory.getFailedUsers())
                .totalRecommendations(batchHistory.getTotalRecommendations())
                .executionTimeMs(batchHistory.getExecutionTimeMs())
                .errorMessage(batchHistory.getErrorMessage())
                .build();
    }
}
