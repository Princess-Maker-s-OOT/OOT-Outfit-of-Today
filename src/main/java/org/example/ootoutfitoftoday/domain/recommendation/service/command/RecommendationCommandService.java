package org.example.ootoutfitoftoday.domain.recommendation.service.command;

import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationCreateResponse;

import java.util.List;

public interface RecommendationCommandService {

    /**
     * 사용자에게 기부/판매 추천 기록을 생성
     * <p>
     * 초기 성능 기준선 확보를 위해 동기적으로 구현
     * (추후 Spring Batch로 고도화될 예정)
     *
     * @param userId 추천을 생성할 대상 사용자 ID
     * @return 생성된 추천 기록의 DTO 목록
     */
    List<RecommendationCreateResponse> generateRecommendations(Long userId);
}