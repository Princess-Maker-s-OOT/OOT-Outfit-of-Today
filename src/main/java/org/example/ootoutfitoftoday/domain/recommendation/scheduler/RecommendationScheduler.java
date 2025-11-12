package org.example.ootoutfitoftoday.domain.recommendation.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.recommendation.entity.RecommendationBatchHistory;
import org.example.ootoutfitoftoday.domain.recommendation.service.batch.RecommendationBatchHistoryService;
import org.example.ootoutfitoftoday.domain.recommendation.service.command.RecommendationCommandService;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

    private static final int PAGE_SIZE = 100; // 100명씩 페이징 처리

    private final RecommendationCommandService recommendationCommandService;
    private final RecommendationBatchHistoryService batchHistoryService;
    private final UserQueryService userQueryService;

    /**
     * 매일 새벽 2시에 추천 배치 실행
     * cron 표현식: 초 분 시 일 월 요일
     * 0 0 2 * * * = 매일 2시 0분 0초
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void generateDailyRecommendations() {
        log.info("=== 추천 배치 시작 ===");

        // 배치 이력 시작 기록
        RecommendationBatchHistory batchHistory = batchHistoryService.startBatch();

        AtomicInteger totalUsers = new AtomicInteger(0);
        AtomicInteger successUsers = new AtomicInteger(0);
        AtomicInteger failedUsers = new AtomicInteger(0);
        AtomicInteger totalRecommendations = new AtomicInteger(0);

        try {
            int pageNumber = 0;
            Page<Long> userIdsPage;

            // 사용자를 100명씩 페이징하여 순차 처리
            do {
                userIdsPage = userQueryService.findAllActiveUserIds(PageRequest.of(pageNumber, PAGE_SIZE));

                log.info("Processing page {}: {} users", pageNumber, userIdsPage.getNumberOfElements());

                // 각 사용자에 대해 추천 생성
                for (Long userId : userIdsPage.getContent()) {
                    totalUsers.incrementAndGet();

                    try {
                        int recommendationCount = recommendationCommandService
                                .generateRecommendations(userId)
                                .size();

                        totalRecommendations.addAndGet(recommendationCount);
                        successUsers.incrementAndGet();

                        if (recommendationCount > 0) {
                            log.debug("Generated {} recommendations for user {}", recommendationCount, userId);
                        }

                    } catch (Exception e) {
                        failedUsers.incrementAndGet();
                        log.error("Failed to generate recommendations for user {}: {}", userId, e.getMessage(), e);
                    }
                }

                pageNumber++;

            } while (userIdsPage.hasNext());

            // 배치 성공 기록
            batchHistoryService.completeBatchSuccess(
                    batchHistory.getId(),
                    totalUsers.get(),
                    successUsers.get(),
                    failedUsers.get(),
                    totalRecommendations.get()
            );

            log.info("=== 추천 배치 완료 === Total: {}, Success: {}, Failed: {}, Recommendations: {}",
                    totalUsers.get(), successUsers.get(), failedUsers.get(), totalRecommendations.get());

        } catch (Exception e) {
            log.error("Batch execution failed critically", e);

            // 배치 실패 기록
            batchHistoryService.completeBatchFailure(
                    batchHistory.getId(),
                    e.getMessage()
            );

            throw new RuntimeException("Recommendation batch failed", e);
        }
    }
}