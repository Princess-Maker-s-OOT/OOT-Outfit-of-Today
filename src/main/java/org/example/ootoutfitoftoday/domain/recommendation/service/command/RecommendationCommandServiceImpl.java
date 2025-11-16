package org.example.ootoutfitoftoday.domain.recommendation.service.command;

import com.ootcommon.recommendation.status.RecommendationStatus;
import com.ootcommon.recommendation.type.RecommendationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.example.ootoutfitoftoday.domain.recommendation.dto.request.RecommendationSalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationCreateResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationErrorCode;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationException;
import org.example.ootoutfitoftoday.domain.recommendation.repository.RecommendationRepository;
import org.example.ootoutfitoftoday.domain.recommendation.service.query.RecommendationQueryService;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.service.command.SalePostCommandService;
import org.example.ootoutfitoftoday.domain.salepost.service.query.SalePostQueryService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationCommandServiceImpl implements RecommendationCommandService {

    private static final String UNWORN_REASON = "마지막 착용일이 1년 이상 경과";

    private final RecommendationRepository recommendationRepository;
    private final RecommendationQueryService recommendationQueryService;
    private final SalePostCommandService salePostCommandService;
    private final SalePostQueryService salePostQueryService;
    private final ClothesQueryService clothesQueryService;
    private final UserQueryService userQueryService;
    private final Clock clock;

    // 사용자에게 기부/판매 추천 기록을 생성
    @Override
    public List<RecommendationCreateResponse> generateRecommendations(Long userId) {
        log.info("Generating recommendations for userId: {}", userId);

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);
        log.debug("User found: {}", user.getId());

        List<Clothes> clothesList = clothesQueryService.findAllClothesByUserId(userId);
        log.debug("Total clothes found for user {}: {}", userId, clothesList.size());

        // 추천 조건 검사 + 엔티티 생성 (각 옷마다 판매/기부 2개의 추천 생성)
        List<Recommendation> recommendations = clothesList.stream()
                .filter(this::isUnwornForOneYear)
                .flatMap(clothes -> Arrays.stream(RecommendationType.values())
                        .map(type -> Recommendation.createForUnwornClothes(
                                user,
                                clothes,
                                type,
                                UNWORN_REASON
                        )))
                .toList();

        log.debug("Recommendations to be saved: {}", recommendations.size());

        List<Recommendation> savedRecommendations = recommendationRepository.saveAll(recommendations);
        log.info("Successfully saved {} recommendations for userId: {}", savedRecommendations.size(), userId);

        return savedRecommendations.stream()
                .map(RecommendationCreateResponse::from)
                .toList();
    }

    // Spring Batch용: 추천 엔티티만 생성 (저장하지 않음)
    @Override
    public List<Recommendation> createRecommendationsForBatch(Long userId) {
        log.info("Creating batch recommendations for userId: {}", userId);

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);
        log.debug("User found for batch: {}", user.getId());

        List<Clothes> clothesList = clothesQueryService.findAllClothesByUserId(userId);
        log.debug("Total clothes found for batch user {}: {}", userId, clothesList.size());

        long unwornClothesCount = clothesList.stream()
                .filter(this::isUnwornForOneYear)
                .count();
        log.debug("Unworn clothes (1+ year) for user {}: {}", userId, unwornClothesCount);

        // 추천 조건 검사 + 엔티티 생성 (각 옷마다 판매/기부 2개의 추천 생성)
        List<Recommendation> recommendations = clothesList.stream()
                .filter(this::isUnwornForOneYear)
                .flatMap(clothes -> Arrays.stream(RecommendationType.values())
                        .map(type -> Recommendation.createForUnwornClothes(
                                user,
                                clothes,
                                type,
                                UNWORN_REASON
                        )))
                .toList();

        log.info("Created {} batch recommendations for userId: {} (unsaved)", recommendations.size(), userId);
        return recommendations;
    }

    // 마지막 착용일이 1년 이상 경과했는지 확인
    private boolean isUnwornForOneYear(Clothes clothes) {
        LocalDateTime lastWornAt = clothes.getLastWornAt();

        // lastWornAt이 null이면 착용한 적이 없으므로 추천 대상
        if (lastWornAt == null) {
            log.trace("Clothes {} has never been worn (lastWornAt is null), eligible for recommendation",
                    clothes.getId());
            return true;
        }

        // lastWornAt을 LocalDate로 변환하여 1년 전과 비교
        LocalDate lastWornDate = lastWornAt.toLocalDate();

        LocalDate oneYearAgo = LocalDate.now(clock).minusYears(1);

        boolean isUnworn = lastWornDate.isBefore(oneYearAgo);
        if (isUnworn) {
            log.trace("Clothes {} last worn on {}, which is before {}, eligible for recommendation",
                    clothes.getId(), lastWornDate, oneYearAgo);
        }

        return isUnworn;
    }

    // 추천으로부터 판매글 생성
    @Override
    public SalePostCreateResponse createSalePostFromRecommendation(
            Long recommendationId,
            Long userId,
            RecommendationSalePostCreateRequest request
    ) {
        log.info("Creating sale post from recommendation: {}, userId: {}", recommendationId, userId);

        Recommendation recommendation = recommendationQueryService.findById(recommendationId);
        log.debug("Found recommendation: {}, status: {}, type: {}, ownerId: {}",
                recommendationId, recommendation.getStatus(), recommendation.getType(), recommendation.getUser().getId());

        if (recommendation.getStatus() != RecommendationStatus.ACCEPTED) {
            log.warn("Recommendation is not ACCEPTED - recommendationId: {}, status: {}",
                    recommendationId, recommendation.getStatus());
            throw new RecommendationException(RecommendationErrorCode.RECOMMENDATION_NOT_ACCEPTED);
        }

        if (recommendation.getType() != RecommendationType.SALE) {
            log.warn("Recommendation is not SALE type - recommendationId: {}, type: {}",
                    recommendationId, recommendation.getType());
            throw new RecommendationException(RecommendationErrorCode.RECOMMENDATION_NOT_SALE_TYPE);
        }

        if (!recommendation.getUser().getId().equals(userId)) {
            log.warn("Unauthorized access to recommendation - recommendationId: {}, requestUserId: {}, ownerUserId: {}",
                    recommendationId, userId, recommendation.getUser().getId());
            throw new RecommendationException(RecommendationErrorCode.RECOMMENDATION_NOT_FOUND);
        }

        Optional<SalePost> existingSalePost = salePostQueryService.findByRecommendationId(recommendationId);

        if (existingSalePost.isPresent()) {
            log.info("Sale post already exists for recommendation - recommendationId: {}, salePostId: {}",
                    recommendationId, existingSalePost.get().getId());

            return SalePostCreateResponse.from(existingSalePost.get());
        }

        log.debug("Creating new sale post from recommendation: {}, clothesId: {}",
                recommendationId, recommendation.getClothes().getId());

        SalePostCreateResponse response = salePostCommandService.createSalePostFromRecommendation(
                recommendation,
                request.categoryId(),
                request.title(),
                request.content(),
                request.price(),
                request.tradeAddress(),
                request.tradeLatitude(),
                request.tradeLongitude(),
                request.imageUrls()
        );

        log.info("Successfully created sale post from recommendation: {}, salePostId: {}",
                recommendationId, response.getSalePostId());

        return response;
    }
}