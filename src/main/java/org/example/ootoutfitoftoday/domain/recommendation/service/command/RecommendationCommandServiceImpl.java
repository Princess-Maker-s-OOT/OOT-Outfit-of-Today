package org.example.ootoutfitoftoday.domain.recommendation.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationCreateResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.recommendation.repository.RecommendationRepository;
import org.example.ootoutfitoftoday.domain.recommendation.type.RecommendationType;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationCommandServiceImpl implements RecommendationCommandService {

    private static final String UNWORN_REASON = "마지막 착용일이 1년 이상 경과";

    private final RecommendationRepository recommendationRepository;
    private final ClothesQueryService clothesQueryService;
    private final UserQueryService userQueryService;
    private final Clock clock;

    // 사용자에게 기부/판매 추천 기록을 생성
    @Override
    public List<RecommendationCreateResponse> generateRecommendations(Long userId) {

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        List<Clothes> clothesList = clothesQueryService.findAllClothesByUserId(userId);

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

        List<Recommendation> savedRecommendations = recommendationRepository.saveAll(recommendations);

        return savedRecommendations.stream()
                .map(RecommendationCreateResponse::from)
                .toList();
    }

    // 마지막 착용일이 1년 이상 경과했는지 확인
    private boolean isUnwornForOneYear(Clothes clothes) {
        LocalDateTime lastWornAt = clothes.getLastWornAt();

        // lastWornAt이 null이면 착용한 적이 없으므로 추천 대상
        if (lastWornAt == null) {

            return true;
        }

        // lastWornAt을 LocalDate로 변환하여 1년 전과 비교
        LocalDate lastWornDate = lastWornAt.toLocalDate();

        LocalDate oneYearAgo = LocalDate.now(clock).minusYears(1);

        return lastWornDate.isBefore(oneYearAgo);
    }
}