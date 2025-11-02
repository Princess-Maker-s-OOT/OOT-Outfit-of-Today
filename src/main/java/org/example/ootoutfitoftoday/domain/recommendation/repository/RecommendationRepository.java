package org.example.ootoutfitoftoday.domain.recommendation.repository;

import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    /**
     * 특정 사용자의 추천 목록을 페이징하여 조회
     * N+1 문제 방지를 위해 JOIN FETCH 사용
     */
    @Query(value = "SELECT DISTINCT r FROM Recommendation r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.clothes c " +
            "LEFT JOIN FETCH c.images ci " +
            "LEFT JOIN FETCH ci.image i " +
            "WHERE u.id = :userId",
            countQuery = "SELECT count(r) FROM Recommendation r WHERE r.user.id = :userId")
    Page<Recommendation> findMyRecommendationsWithClothes(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
