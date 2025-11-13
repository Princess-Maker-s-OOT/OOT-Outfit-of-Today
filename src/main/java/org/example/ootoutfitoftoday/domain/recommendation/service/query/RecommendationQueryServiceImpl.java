package org.example.ootoutfitoftoday.domain.recommendation.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;
import org.example.ootoutfitoftoday.domain.donation.service.query.DonationCenterQueryService;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationErrorCode;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationException;
import org.example.ootoutfitoftoday.domain.recommendation.repository.RecommendationRepository;
import org.example.ootoutfitoftoday.domain.recommendation.status.RecommendationStatus;
import org.example.ootoutfitoftoday.domain.recommendation.type.RecommendationType;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
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
    private final UserQueryService userQueryService;
    private final DonationCenterQueryService donationCenterQueryService;

    /**
     * 내 추천 기록 리스트 조회 (2단계 쿼리 최적화)
     * 1단계: 페이징을 적용하여 추천 ID 목록만 조회 (DB 레벨 페이징)
     * 2단계: 조회된 ID로 JOIN FETCH를 통한 전체 엔티티 그래프 로드
     * 이 방식은 컬렉션 JOIN FETCH + 페이징 시 발생하는 HHH000104 경고와
     * OutOfMemoryError를 방지하여 대용량 데이터에서도 안정적인 성능 보장
     */
    @Override
    public Page<RecommendationGetMyResponse> getMyRecommendations(
            Long userId,
            Pageable pageable
    ) {
        Page<Recommendation> idsPage = recommendationRepository.findRecommendationIdsByUserId(
                userId,
                pageable
        );

        if (idsPage.isEmpty()) {

            return Page.empty(pageable);
        }

        List<Long> ids = idsPage.getContent().stream()
                .map(Recommendation::getId)
                .toList();

        // ID 목록으로 JOIN FETCH를 통한 전체 엔티티 그래프 로드 (N+1 방지)
        List<Recommendation> recommendations =
                recommendationRepository.findRecommendationsWithDetailsByIds(ids);

        Map<Long, Recommendation> recommendationMap = recommendations.stream()
                .collect(Collectors.toMap(Recommendation::getId, r -> r));

        // 원본 페이지의 정렬 순서를 유지하면서 상세 정보가 포함된 엔티티로 변환
        List<RecommendationGetMyResponse> content = ids.stream()
                .map(recommendationMap::get)
                .map(RecommendationGetMyResponse::from)
                .toList();

        // 페이징 메타데이터를 유지하면서 새로운 Page 객체 생성
        return new PageImpl<>(
                content,
                pageable,
                idsPage.getTotalElements()
        );
    }

    /**
     * 추천 ID로 추천 조회
     *
     * @param recommendationId 조회할 추천 ID
     * @return 조회된 추천 엔티티
     * @throws RecommendationException 추천을 찾을 수 없는 경우
     */
    @Override
    public Recommendation findById(Long recommendationId) {

        return recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RecommendationException(RecommendationErrorCode.RECOMMENDATION_NOT_FOUND));
    }

    /**
     * 추천으로부터 기부처 검색
     * ACCEPTED 상태의 DONATION 타입 추천에서 사용자 위치 기반으로 주변 기부처를 검색합니다.
     *
     * @param recommendationId 추천 ID
     * @param userId           사용자 ID
     * @param radius           검색 반경 (미터)
     * @param keyword          검색 키워드
     * @return 거리순으로 정렬된 기부처 목록
     * @throws RecommendationException 추천을 찾을 수 없거나, 권한이 없거나, 상태/타입이 맞지 않는 경우
     */
    @Override
    public List<DonationCenterSearchResponse> searchDonationCentersFromRecommendation(
            Long recommendationId,
            Long userId,
            Integer radius,
            String keyword
    ) {
        Recommendation recommendation = findById(recommendationId);

        if (!recommendation.getUser().getId().equals(userId)) {
            throw new RecommendationException(RecommendationErrorCode.RECOMMENDATION_NOT_FOUND);
        }

        if (recommendation.getStatus() != RecommendationStatus.ACCEPTED) {
            throw new RecommendationException(RecommendationErrorCode.RECOMMENDATION_NOT_ACCEPTED);
        }

        if (recommendation.getType() != RecommendationType.DONATION) {
            throw new RecommendationException(RecommendationErrorCode.RECOMMENDATION_NOT_DONATION_TYPE);
        }

        User user = userQueryService.findByIdAsNativeQuery(userId);

        if (user.getTradeLocation() == null || user.getTradeLocation().isEmpty()) {
            throw new RecommendationException(RecommendationErrorCode.USER_LOCATION_NOT_FOUND);
        }

        double[] coordinates = parseTradeLocation(user.getTradeLocation());
        Double longitude = coordinates[0];
        Double latitude = coordinates[1];

        return donationCenterQueryService.searchNearbyDonationCenters(
                latitude,
                longitude,
                radius,
                keyword
        );
    }

    /**
     * tradeLocation 문자열에서 위도/경도 파싱
     * tradeLocation은 POINT(latitude longitude) 형식으로 저장됨 (latitude가 먼저 옴)
     * 예: POINT(37.5665 126.9780)
     *
     * @param tradeLocation POINT 형식의 위치 문자열
     * @return [0]: longitude, [1]: latitude (DonationCenterQueryService에 전달하기 위한 순서)
     * @throws RecommendationException 파싱 실패 시
     */
    private double[] parseTradeLocation(String tradeLocation) {
        try {
            String locationStr = tradeLocation
                    .replace("POINT(", "")
                    .replace(")", "")
                    .trim();

            String[] coords = locationStr.split("\\s+");

            if (coords.length != 2) {
                throw new RecommendationException(RecommendationErrorCode.INVALID_USER_LOCATION_FORMAT);
            }

            double latitude = Double.parseDouble(coords[0]);
            double longitude = Double.parseDouble(coords[1]);

            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                throw new RecommendationException(RecommendationErrorCode.INVALID_USER_LOCATION_FORMAT);
            }

            return new double[]{longitude, latitude};

        } catch (NumberFormatException e) {
            throw new RecommendationException(RecommendationErrorCode.INVALID_USER_LOCATION_FORMAT);
        }
    }
}