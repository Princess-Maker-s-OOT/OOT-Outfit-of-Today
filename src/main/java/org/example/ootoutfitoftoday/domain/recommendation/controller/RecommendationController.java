package org.example.ootoutfitoftoday.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationCreateResponse;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationSuccessCode;
import org.example.ootoutfitoftoday.domain.recommendation.service.command.RecommendationCommandService;
import org.example.ootoutfitoftoday.domain.recommendation.service.query.RecommendationQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "추천 기록 관리", description = "기부/판매 추천 기록 생성 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recommendations")
public class RecommendationController {

    private final RecommendationCommandService recommendationCommandService;
    private final RecommendationQueryService recommendationQueryService;

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

    /**
     * 추천 기록 목록 조회
     * <p>
     * 로그인한 사용자의 기부/판매 추천 목록을 페이징하여 조회
     * 기본 정렬: 생성일시 최신순 (createdAt DESC)
     *
     * @param authUser  인증된 사용자 정보
     * @param page      페이지 번호 (0부터 시작, 기본값 0)
     * @param size      페이지 크기 (기본값 10)
     * @param sort      정렬 기준 필드 (기본값 createdAt)
     * @param direction 정렬 방향 (ASC/DESC, 기본값 DESC)
     * @return 추천 목록 페이지 응답
     */
    @Operation(
            summary = "추천 기록 목록 조회",
            description = """
                    로그인한 사용자의 기부/판매 추천 목록을 페이징하여 조회합니다.
                    기본적으로 생성일시 기준 최신순으로 정렬됩니다.
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "500", description = "서버 로직 오류")
            }
    )
    @GetMapping
    public ResponseEntity<PageResponse<RecommendationGetMyResponse>> getMyRecommendations(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sort)
        );

        Page<RecommendationGetMyResponse> responsePage = recommendationQueryService.getMyRecommendations(
                authUser.getUserId(),
                pageable
        );

        return PageResponse.success(responsePage, RecommendationSuccessCode.RECOMMENDATION_GET_OK);
    }
}