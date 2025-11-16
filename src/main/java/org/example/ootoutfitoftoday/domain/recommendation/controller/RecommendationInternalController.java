package org.example.ootoutfitoftoday.domain.recommendation.controller;

import com.ootcommon.recommendation.dto.RecommendationBatchCreateResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationSuccessCode;
import org.example.ootoutfitoftoday.domain.recommendation.service.command.RecommendationCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 배치 서버 전용 Internal API Controller
 * 배치 서버에서 메인 서버의 추천 생성 로직을 호출하기 위한 API를 제공합니다.
 * <p>
 * 보안 참고사항:
 * - /v1/internal/** 경로는 Spring Security에서 permitAll() 처리됨
 * - 프로덕션 환경에서는 네트워크 레벨 접근 제어(IP 화이트리스트 등) 필요
 * - 배치 서버 외부에서의 접근을 방지하기 위한 추가 보안 조치 권장
 */
@Slf4j
@Hidden // Swagger UI에서 숨김 (내부 API이므로)
@Tag(name = "Internal API", description = "배치 서버 전용 Internal API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/internal/batch/recommendations")
public class RecommendationInternalController {

    private final RecommendationCommandService recommendationCommandService;

    /**
     * 배치용 추천 생성
     * 배치 서버의 RecommendationItemProcessor에서 호출하여
     * 특정 사용자에 대한 추천을 생성
     * 생성된 추천 데이터는 DTO 형태로 반환되며,
     * 배치 서버의 Writer에서 이 데이터를 받아 저장
     *
     * @param userId 추천을 생성할 대상 사용자 ID
     * @return 생성된 추천 데이터 목록 (배치 서버에서 저장에 사용)
     */
    @Operation(
            summary = "[Internal] 배치용 추천 생성",
            description = """
                    배치 서버의 Processor에서 호출하는 Internal API입니다.
                    
                    특정 사용자에 대해 1년 이상 미착용 옷을 조회하고
                    판매/기부 추천 데이터를 생성하여 반환합니다.
                    
                    반환된 데이터는 배치 서버의 Writer에서 저장됩니다.
                    
                    주의: 이 API는 배치 서버 전용이며, 외부 접근이 제한되어야 합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "201", description = "추천 생성 성공"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    @PostMapping("/users/{userId}")
    public ResponseEntity<Response<List<RecommendationBatchCreateResponse>>> createRecommendationsForBatch(
            @PathVariable Long userId
    ) {
        log.info("[Internal API] Creating recommendations for user: {}", userId);

        List<Recommendation> recommendations =
                recommendationCommandService.createRecommendationsForBatch(userId);

        log.info("[Internal API] Generated {} recommendations for user: {}", recommendations.size(), userId);

        // 미저장 상태의 엔티티를 DTO로 변환하여 배치 서버에 전달
        // 배치 서버의 Writer에서 이 데이터를 받아 실제 저장을 수행
        List<RecommendationBatchCreateResponse> responseList = recommendations.stream()
                .map(rec -> RecommendationBatchCreateResponse.of(
                        rec.getUser().getId(),
                        rec.getClothes().getId(),
                        rec.getType().name(),
                        rec.getReason(),
                        rec.getStatus().name()
                ))
                .toList();

        return Response.success(responseList, RecommendationSuccessCode.RECOMMENDATION_CREATED);
    }

    /**
     * Internal API 헬스체크
     * 배치 서버에서 메인 서버 연결 상태를 확인하기 위한 엔드포인트
     *
     * @return 상태 메시지
     */
    @Operation(
            summary = "[Internal] 헬스체크",
            description = "배치 서버에서 메인 서버 연결 상태를 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상")
            }
    )
    @GetMapping("/health")
    public ResponseEntity<Response<String>> healthCheck() {
        log.debug("[Internal API] Health check requested");

        return Response.success("Internal API is healthy", RecommendationSuccessCode.RECOMMENDATION_GET_OK);
    }
}