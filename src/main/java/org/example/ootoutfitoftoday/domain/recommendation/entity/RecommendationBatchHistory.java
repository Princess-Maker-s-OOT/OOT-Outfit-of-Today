package org.example.ootoutfitoftoday.domain.recommendation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.recommendation.status.BatchStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "recommendation_batch_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationBatchHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 배치 시작 시각
    @Column(nullable = false)
    private LocalDateTime startTime;

    // 배치 종료 시각
    private LocalDateTime endTime;

    // 배치 상태 (RUNNING, SUCCESS, FAILED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

    // 처리된 총 사용자 수
    @Column(nullable = false)
    private Integer totalUsers;

    // 성공한 사용자 수
    @Column(nullable = false)
    private Integer successUsers;

    // 실패한 사용자 수
    @Column(nullable = false)
    private Integer failedUsers;

    // 생성된 추천 건수
    @Column(nullable = false)
    private Integer totalRecommendations;

    // 실행 시간 (밀리초)
    private Long executionTimeMs;

    // 에러 메시지 (실패 시)
    @Column(length = 1000)
    private String errorMessage;

    @Builder(access = AccessLevel.PRIVATE)
    private RecommendationBatchHistory(
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
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.totalUsers = totalUsers;
        this.successUsers = successUsers;
        this.failedUsers = failedUsers;
        this.totalRecommendations = totalRecommendations;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
    }

    // 배치 시작 시 초기화
    public static RecommendationBatchHistory createInitial(LocalDateTime startTime) {

        return RecommendationBatchHistory.builder()
                .startTime(startTime)
                .status(BatchStatus.RUNNING)
                .totalUsers(0)
                .successUsers(0)
                .failedUsers(0)
                .totalRecommendations(0)
                .build();
    }

    // 배치 성공 처리
    public void markAsSuccess(
            LocalDateTime endTime,
            Integer totalUsers,
            Integer successUsers,
            Integer failedUsers,
            Integer totalRecommendations
    ) {
        this.endTime = endTime;
        this.status = BatchStatus.SUCCESS;
        this.totalUsers = totalUsers;
        this.successUsers = successUsers;
        this.failedUsers = failedUsers;
        this.totalRecommendations = totalRecommendations;
        updateExecutionTime(endTime);
    }

    // 배치 성공 처리 (Spring Batch용 - 실행 시간 직접 지정)
    public void markAsSuccess(

            LocalDateTime endTime,
            Integer totalUsers,
            Integer successUsers,
            Integer failedUsers,
            Integer totalRecommendations,
            Long executionTimeMs
    ) {
        this.endTime = endTime;
        this.status = BatchStatus.SUCCESS;
        this.totalUsers = totalUsers;
        this.successUsers = successUsers;
        this.failedUsers = failedUsers;
        this.totalRecommendations = totalRecommendations;
        this.executionTimeMs = executionTimeMs;
    }

    // 배치 실패 처리
    public void markAsFailed(LocalDateTime endTime, String errorMessage) {

        this.endTime = endTime;
        this.status = BatchStatus.FAILED;
        updateExecutionTime(endTime);
        this.errorMessage = errorMessage;
    }

    // 배치 실패 처리 (Spring Batch용 - 실행 시간 직접 지정)
    public void markAsFailed(
            
            LocalDateTime endTime,
            Long executionTimeMs,
            String errorMessage
    ) {
        this.endTime = endTime;
        this.status = BatchStatus.FAILED;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
    }

    // 실행 시간 계산 헬퍼 메서드
    private void updateExecutionTime(LocalDateTime endTime) {

        this.executionTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
    }
}