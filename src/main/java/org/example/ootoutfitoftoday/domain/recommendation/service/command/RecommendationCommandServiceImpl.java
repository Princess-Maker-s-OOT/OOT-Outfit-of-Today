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

    @Override
    public List<RecommendationCreateResponse> generateRecommendations(Long userId) {
        log.info("추천 생성 시작 - 사용자: {}", userId);

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);
        log.debug("사용자 조회 완료 - 사용자: {}", user.getId());

        List<Clothes> clothesList = clothesQueryService.findAllClothesByUserId(userId);
        log.debug("전체 옷 조회 완료 - 사용자: {}, 옷 개수: {}", userId, clothesList.size());

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

        log.debug("저장 예정 추천 건수: {}", recommendations.size());

        List<Recommendation> savedRecommendations = recommendationRepository.saveAll(recommendations);
        log.info("추천 저장 완료 - 저장 건수: {}, 사용자: {}", savedRecommendations.size(), userId);

        return savedRecommendations.stream()
                .map(RecommendationCreateResponse::from)
                .toList();
    }

    @Override
    public List<Recommendation> createRecommendationsForBatch(Long userId) {
        log.info("배치 추천 생성 시작 - 사용자: {}", userId);

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);
        log.debug("배치용 사용자 조회 완료 - 사용자: {}", user.getId());

        List<Clothes> clothesList = clothesQueryService.findAllClothesByUserId(userId);
        log.debug("배치용 전체 옷 조회 완료 - 사용자: {}, 옷 개수: {}", userId, clothesList.size());

        long unwornClothesCount = clothesList.stream()
                .filter(this::isUnwornForOneYear)
                .count();
        log.debug("1년 이상 미착용 옷 개수 - 사용자: {}, 개수: {}", userId, unwornClothesCount);

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

        log.info("배치 추천 생성 완료 (미저장) - 생성 건수: {}, 사용자: {}", recommendations.size(), userId);

        return recommendations;
    }

    private boolean isUnwornForOneYear(Clothes clothes) {
        LocalDateTime lastWornAt = clothes.getLastWornAt();

        if (lastWornAt == null) {
            log.trace("착용 기록 없음 (추천 대상) - 옷ID: {}", clothes.getId());
            return true;
        }

        LocalDate lastWornDate = lastWornAt.toLocalDate();

        LocalDate oneYearAgo = LocalDate.now(clock).minusYears(1);

        boolean isUnworn = lastWornDate.isBefore(oneYearAgo);
        if (isUnworn) {
            log.trace("1년 이상 미착용 (추천 대상) - 옷ID: {}, 마지막착용: {}, 기준일: {}",
                    clothes.getId(), lastWornDate, oneYearAgo);
        }

        return isUnworn;
    }

    @Override
    public SalePostCreateResponse createSalePostFromRecommendation(
            Long recommendationId,
            Long userId,
            RecommendationSalePostCreateRequest request
    ) {
        log.info("추천 기반 판매글 생성 시작 - 추천ID: {}, 사용자: {}", recommendationId, userId);

        Recommendation recommendation = recommendationQueryService.findById(recommendationId);
        log.debug("추천 조회 완료 - 추천ID: {}, 상태: {}, 타입: {}, 소유자: {}",
                recommendationId, recommendation.getStatus(), recommendation.getType(), recommendation.getUser().getId());

        if (recommendation.getStatus() != RecommendationStatus.ACCEPTED) {
            log.warn("추천 상태가 ACCEPTED가 아님 - 추천ID: {}, 상태: {}",
                    recommendationId, recommendation.getStatus());
            throw new RecommendationException(RecommendationErrorCode.RECOMMENDATION_NOT_ACCEPTED);
        }

        if (recommendation.getType() != RecommendationType.SALE) {
            log.warn("추천 타입이 SALE이 아님 - 추천ID: {}, 타입: {}",
                    recommendationId, recommendation.getType());
            throw new RecommendationException(RecommendationErrorCode.RECOMMENDATION_NOT_SALE_TYPE);
        }

        if (!recommendation.getUser().getId().equals(userId)) {
            log.warn("추천 접근 권한 없음 - 추천ID: {}, 요청사용자: {}, 소유자: {}",
                    recommendationId, userId, recommendation.getUser().getId());
            throw new RecommendationException(RecommendationErrorCode.RECOMMENDATION_NOT_FOUND);
        }

        Optional<SalePost> existingSalePost = salePostQueryService.findByRecommendationId(recommendationId);

        if (existingSalePost.isPresent()) {
            log.info("기존 판매글 존재 - 추천ID: {}, 판매글ID: {}",
                    recommendationId, existingSalePost.get().getId());

            return SalePostCreateResponse.from(existingSalePost.get());
        }

        log.debug("신규 판매글 생성 - 추천ID: {}, 옷ID: {}",
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
                request.imageIds()
        );

        log.info("판매글 생성 완료 - 추천ID: {}, 판매글ID: {}",
                recommendationId, response.getSalePostId());

        return response;
    }
}