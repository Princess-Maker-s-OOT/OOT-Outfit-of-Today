package org.example.ootoutfitoftoday.domain.recommendation.repository;

import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}
