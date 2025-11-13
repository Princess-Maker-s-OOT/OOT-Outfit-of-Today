package org.example.ootoutfitoftoday.domain.recommendation.batch.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.recommendation.batch.dto.RecommendationBatchResult;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.recommendation.repository.RecommendationRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 생성된 추천을 DB에 저장하는 Writer
 * Chunk 단위로 추천을 배치 저장하여 성능을 최적화합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationItemWriter implements ItemWriter<RecommendationBatchResult> {

    private final RecommendationRepository recommendationRepository;

    @Override
    public void write(Chunk<? extends RecommendationBatchResult> chunk) {

        List<Recommendation> allRecommendations = chunk.getItems().stream()
                .filter(RecommendationBatchResult::success)
                .flatMap(result -> result.recommendations().stream())
                .toList();

        if (!allRecommendations.isEmpty()) {
            recommendationRepository.saveAll(allRecommendations);
            log.debug("Saved {} recommendations in chunk", allRecommendations.size());
        }

        // 실패 케이스 로깅
        chunk.getItems().stream()
                .filter(result -> !result.success())
                .forEach(result -> log.warn(
                        "Failed to generate recommendations for user {}: {}",
                        result.userId(),
                        result.errorMessage()
                ));
    }
}
