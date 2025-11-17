package org.example.ootoutfitoftoday.domain.recommendation.service.command;

import org.example.ootoutfitoftoday.domain.recommendation.dto.request.RecommendationSalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationCreateResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;

import java.util.List;

public interface RecommendationCommandService {

    /**
     * 사용자에게 기부/판매 추천 기록을 생성
     * 초기 성능 기준선 확보를 위해 동기적으로 구현
     *
     * @param userId 추천을 생성할 대상 사용자 ID
     * @return 생성된 추천 기록의 DTO 목록
     */
    List<RecommendationCreateResponse> generateRecommendations(Long userId);

    /**
     * Spring Batch용: 추천 엔티티만 생성 (저장하지 않음)
     * Processor에서 호출되며, Writer에서 실제 저장이 이루어짐
     *
     * @param userId 추천을 생성할 대상 사용자 ID
     * @return 생성된 추천 엔티티 목록 (미저장 상태)
     */
    List<Recommendation> createRecommendationsForBatch(Long userId);

    /**
     * 추천 수락 상태에서 판매글 생성
     * ACCEPTED 상태의 판매 추천으로부터 판매글을 생성
     * 중복 생성을 방지하며, 이미 존재하는 경우 기존 판매글을 반환
     *
     * @param recommendationId 판매글을 생성할 추천 ID
     * @param userId           요청 사용자 ID
     * @param request          판매글 생성 요청 정보
     * @return 생성되거나 조회된 판매글 정보
     */
    SalePostCreateResponse createSalePostFromRecommendation(
            Long recommendationId,
            Long userId,
            RecommendationSalePostCreateRequest request
    );
}