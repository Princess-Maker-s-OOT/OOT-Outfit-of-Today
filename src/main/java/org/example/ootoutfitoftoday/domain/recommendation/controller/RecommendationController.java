package org.example.ootoutfitoftoday.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationCreateResponse;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationSuccessCode;
import org.example.ootoutfitoftoday.domain.recommendation.service.command.RecommendationCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "추천 기록 관리", description = "기부/판매 추천 기록 생성 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recommendations")
public class RecommendationController {

    private final RecommendationCommandService recommendationCommandService;

    /**
     * 추천 기록 생성 (수동 호출 및 기준선 측정용)
     * <p>
     * 사용자의 모든 옷을 동기적으로 조회
     * 1년 이상 착용하지 않은 옷에 대해 판매 또는 기부 추천 기록을 생성
     * 생성된 각 추천 기록의 상세 정보를 리스트로 반환
     *
     * @param authUser 인증된 사용자 정보
     * @return List<RecommendationCreateResponse>: 생성된 추천 기록 DTO 목록
     */
    @Operation(
            summary = "추천 기록 수동 생성",
            description = """
                    로그인한 사용자의 옷을 동기적으로 조회하여 1년 이상 미착용 옷에 대한 판매 또는 기부 추천 기록을 생성합니다.
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "추천 기록 생성 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 로직 오류")
            }
    )
    @PostMapping
    public ResponseEntity<Response<List<RecommendationCreateResponse>>> generateRecommendations(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        List<RecommendationCreateResponse> responses = recommendationCommandService.generateRecommendations(authUser.getUserId());

        return Response.success(responses, RecommendationSuccessCode.RECOMMENDATION_CREATED);
    }
}