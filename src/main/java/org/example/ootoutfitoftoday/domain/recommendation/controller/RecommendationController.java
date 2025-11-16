package org.example.ootoutfitoftoday.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;
import org.example.ootoutfitoftoday.domain.recommendation.dto.request.RecommendationSalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationSuccessCode;
import org.example.ootoutfitoftoday.domain.recommendation.service.command.RecommendationCommandService;
import org.example.ootoutfitoftoday.domain.recommendation.service.query.RecommendationQueryService;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "추천 기록 관리", description = "기부/판매 추천 기록 조회 및 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recommendations")
public class RecommendationController {

    private final RecommendationCommandService recommendationCommandService;
    private final RecommendationQueryService recommendationQueryService;

    /**
     * 로그인한 사용자의 추천 목록 조회
     */
    @Operation(
            summary = "추천 기록 목록 조회",
            description = """
                    로그인한 사용자의 기부/판매 추천 목록을 페이징하여 조회합니다.
                    기본적으로 생성일 기준 최신순으로 정렬됩니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
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

    /**
     * 추천으로부터 판매글 생성
     */
    @Operation(
            summary = "추천 → 판매글 생성",
            description = """
                    ACCEPTED 상태의 판매 추천을 기반으로 판매글을 생성합니다.
                    이미 판매글이 존재한다면 기존 글을 반환합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "201", description = "판매글 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 추천 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "추천을 찾을 수 없음"),
                    @ApiResponse(responseCode = "409", description = "이미 판매글 존재")
            }
    )
    @PostMapping("/{recommendationId}/sale-posts")
    public ResponseEntity<Response<SalePostCreateResponse>> createSalePostFromRecommendation(
            @PathVariable Long recommendationId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody RecommendationSalePostCreateRequest request
    ) {
        SalePostCreateResponse response = recommendationCommandService.createSalePostFromRecommendation(
                recommendationId,
                authUser.getUserId(),
                request
        );

        return Response.success(response, RecommendationSuccessCode.SALE_POST_FROM_RECOMMENDATION_CREATED);
    }

    /**
     * 기부 추천에서 주변 기부처 검색
     */
    @Operation(
            summary = "기부 추천 → 주변 기부처 검색",
            description = """
                    ACCEPTED 상태의 기부 추천에서 사용자의 거래 위치 기반으로
                    주변 기부처를 거리순으로 검색합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "기부처 검색 성공"),
                    @ApiResponse(responseCode = "400", description = "기부 타입이 아님"),
                    @ApiResponse(responseCode = "404", description = "추천을 찾을 수 없음")
            }
    )
    @GetMapping("/{recommendationId}/donation-centers")
    public ResponseEntity<Response<List<DonationCenterSearchResponse>>> searchDonationCentersFromRecommendation(
            @PathVariable Long recommendationId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) String keyword
    ) {
        List<DonationCenterSearchResponse> donationCenters = recommendationQueryService.searchDonationCentersFromRecommendation(
                recommendationId,
                authUser.getUserId(),
                radius,
                keyword
        );

        return Response.success(donationCenters, RecommendationSuccessCode.DONATION_CENTER_SEARCH_FROM_RECOMMENDATION_OK);
    }
}