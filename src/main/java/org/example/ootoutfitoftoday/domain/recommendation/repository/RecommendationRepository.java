package org.example.ootoutfitoftoday.domain.recommendation.repository;

import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    /**
     * 1단계: 특정 사용자의 추천 ID 목록을 페이징하여 조회
     * 컬렉션 JOIN FETCH 없이 페이징을 수행하여 메모리 효율성 확보
     */
    @Query("SELECT r FROM Recommendation r " +
            "WHERE r.user.id = :userId")
    Page<Recommendation> findRecommendationIdsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * 2단계: 조회된 ID 목록으로 전체 엔티티 그래프를 JOIN FETCH
     * N+1 문제 방지를 위해 필요한 연관 엔티티를 한 번에 로드
     */
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
