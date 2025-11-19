package org.example.ootoutfitoftoday.domain.recommendation.repository;

import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Query("SELECT r FROM Recommendation r " +
            "WHERE r.user.id = :userId")
    Page<Recommendation> findRecommendationIdsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT r FROM Recommendation r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.clothes c " +
            "LEFT JOIN FETCH c.images ci " +
            "LEFT JOIN FETCH ci.image i " +
            "WHERE r.id IN :ids")
    List<Recommendation> findRecommendationsWithDetailsByIds(
            @Param("ids") List<Long> ids
    );
}