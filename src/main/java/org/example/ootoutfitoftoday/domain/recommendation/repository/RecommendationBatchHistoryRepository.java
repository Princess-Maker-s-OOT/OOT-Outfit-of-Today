package org.example.ootoutfitoftoday.domain.recommendation.repository;

import org.example.ootoutfitoftoday.domain.recommendation.entity.RecommendationBatchHistory;
import org.example.ootoutfitoftoday.domain.recommendation.status.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecommendationBatchHistoryRepository extends JpaRepository<RecommendationBatchHistory, Long> {

    // 최근 배치 이력 조회 (페이징)
    Page<RecommendationBatchHistory> findAllByOrderByStartTimeDesc(Pageable pageable);

    // 특정 기간의 배치 이력 조회
    @Query("SELECT rbh FROM RecommendationBatchHistory rbh " +
            "WHERE rbh.startTime >= :startTime AND rbh.startTime < :endTime " +
            "ORDER BY rbh.startTime DESC")
    List<RecommendationBatchHistory> findByStartTimeBetween(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // 특정 상태의 배치 이력 조회
    List<RecommendationBatchHistory> findByStatusOrderByStartTimeDesc(BatchStatus status);

    // 가장 최근 배치 이력 조회
    Optional<RecommendationBatchHistory> findFirstByOrderByStartTimeDesc();

    // 가장 최근 성공한 배치 이력 조회
    Optional<RecommendationBatchHistory> findFirstByStatusOrderByStartTimeDesc(BatchStatus status);
}