package org.example.ootoutfitoftoday.domain.recommendation.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.recommendation.repository.RecommendationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationQueryServiceImpl implements RecommendationQueryService {

    private final RecommendationRepository recommendationRepository;

    // 내 추천 기록 리스트 조회
    @Override
    public Page<RecommendationGetMyResponse> getMyRecommendations(
            Long userId,
            Pageable pageable
    ) {

        Page<Recommendation> recommendations =
                recommendationRepository.findMyRecommendationsWithClothes(
                        userId,
                        pageable
                );

        return recommendations.map(RecommendationGetMyResponse::from);
    }
}