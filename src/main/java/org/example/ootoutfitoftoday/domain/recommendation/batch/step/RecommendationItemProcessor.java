package org.example.ootoutfitoftoday.domain.recommendation.batch.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.recommendation.batch.dto.RecommendationBatchResult;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.recommendation.service.command.RecommendationCommandService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 사용자 ID를 받아 추천을 생성하는 Processor
 * 각 사용자에 대해:
 * 1. 1년 이상 착용하지 않은 옷 조회
 * 2. 각 옷에 대해 SALE, DONATION 추천 생성
 * 3. 성공/실패 결과를 RecommendationBatchResult로 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationItemProcessor implements ItemProcessor<Long, RecommendationBatchResult> {

    private final RecommendationCommandService recommendationCommandService;

    @Override
    public RecommendationBatchResult process(Long userId) {
        try {
            log.debug("Processing recommendations for user: {}", userId);

            List<Recommendation> recommendations = recommendationCommandService.createRecommendationsForBatch(userId);

            log.debug("Generated {} recommendations for user: {}", recommendations.size(), userId);

            return RecommendationBatchResult.success(userId, recommendations);

        } catch (Exception e) {
            log.error("Failed to process recommendations for user: {}", userId, e);

            return RecommendationBatchResult.failure(userId, e.getMessage());
        }
    }
}