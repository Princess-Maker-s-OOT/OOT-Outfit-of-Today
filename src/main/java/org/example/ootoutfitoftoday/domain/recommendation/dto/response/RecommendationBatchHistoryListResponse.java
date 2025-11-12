package org.example.ootoutfitoftoday.domain.recommendation.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 배치 실행 이력 목록 응답 DTO
 */
@Builder(access = AccessLevel.PRIVATE)
public record RecommendationBatchHistoryListResponse(

        List<RecommendationBatchHistoryResponse> histories,
        int currentPage,
        int totalPages,
        long totalElements,
        boolean hasNext
) {
    public static RecommendationBatchHistoryListResponse from(Page<RecommendationBatchHistoryResponse> page) {

        return RecommendationBatchHistoryListResponse.builder()
                .histories(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .build();
    }
}