package org.example.ootoutfitoftoday.domain.recommendation.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.recommendation.repository.RecommendationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationQueryServiceImpl implements RecommendationQueryService {

    private final RecommendationRepository recommendationRepository;

    /**
     * 내 추천 기록 리스트 조회 (2단계 쿼리 최적화)
     * <p>
     * 1단계: 페이징을 적용하여 추천 ID 목록만 조회 (DB 레벨 페이징)
     * 2단계: 조회된 ID로 JOIN FETCH를 통한 전체 엔티티 그래프 로드
     * <p>
     * 이 방식은 컬렉션 JOIN FETCH + 페이징 시 발생하는 HHH000104 경고와
     * OutOfMemoryError를 방지하여 대용량 데이터에서도 안정적인 성능 보장
     */
    @Override
    public Page<RecommendationGetMyResponse> getMyRecommendations(
            Long userId,
            Pageable pageable
    ) {
        // 1단계: 페이징을 적용하여 추천 ID 목록만 조회 (DB 레벨 LIMIT, OFFSET 적용)
        Page<Recommendation> idsPage = recommendationRepository.findRecommendationIdsByUserId(
                userId,
                pageable
        );

        // 데이터가 없는 경우 빈 페이지 반환
        if (idsPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // 조회된 Recommendation 엔티티에서 ID 목록 추출
        List<Long> ids = idsPage.getContent().stream()
                .map(Recommendation::getId)
                .toList();

        // 2단계: ID 목록으로 JOIN FETCH를 통한 전체 엔티티 그래프 로드 (N+1 방지)
        List<Recommendation> recommendations =
                recommendationRepository.findRecommendationsWithDetailsByIds(ids);

        // ID 기반으로 Map을 생성하여 빠른 조회 가능하도록 최적화
        Map<Long, Recommendation> recommendationMap = recommendations.stream()
                .collect(Collectors.toMap(Recommendation::getId, r -> r));

        // 원본 페이지의 정렬 순서를 유지하면서 상세 정보가 포함된 엔티티로 변환
        List<RecommendationGetMyResponse> content = ids.stream()
                .map(recommendationMap::get)
                .map(RecommendationGetMyResponse::from)
                .toList();

        // 페이징 메타데이터를 유지하면서 새로운 Page 객체 생성
        return new PageImpl<>(content, pageable, idsPage.getTotalElements());
    }
}