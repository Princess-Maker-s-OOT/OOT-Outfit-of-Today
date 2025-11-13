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
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationBatchHistoryListResponse;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationBatchHistoryResponse;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationCreateResponse;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.RecommendationBatchHistory;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationSuccessCode;
import org.example.ootoutfitoftoday.domain.recommendation.service.batch.query.RecommendationBatchHistoryQueryService;
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
@Tag(name = "추천 기록 관리", description = "기부/판매 추천 기록 생성 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recommendations")
public class RecommendationController {

    private final RecommendationCommandService recommendationCommandService;
    private final RecommendationQueryService recommendationQueryService;
    private final RecommendationBatchHistoryQueryService batchHistoryQueryService;

    /**
     * 추천 기록 수동 생성
     * 로그인한 사용자의 옷을 즉시 조회하여 1년 이상 미착용 옷에 대한 추천 기록을 생
     * 스케줄러를 기다리지 않고 즉시 추천을 받고 싶을 때 사용
     * 참고: 매일 새벽 2시에 자동으로 모든 사용자에 대해 배치 실행
     *
     * @param authUser 인증된 사용자 정보
     * @return List<RecommendationCreateResponse>: 생성된 추천 기록 DTO 목록
     */
    @Operation(
            summary = "추천 기록 수동 생성",
            description = """
                    로그인한 사용자의 옷을 즉시 조회하여 1년 이상 미착용 옷에 대한 판매 또는 기부 추천 기록을 생성합니다.
                    
                    스케줄러를 기다리지 않고 즉시 추천을 받고 싶을 때 사용합니다.
                    
                    참고: 매일 새벽 2시에 자동으로 모든 사용자에 대해 배치 실행됩니다.
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

    /**
     * 추천으로부터 판매글 생성
     * ACCEPTED 상태의 판매 추천을 기반으로 판매글을 생성
     * 중복 생성을 방지하며, 이미 판매글이 존재하는 경우 기존 판매글을 반환
     *
     * @param recommendationId 판매글을 생성할 추천 ID
     * @param authUser         인증된 사용자 정보
     * @param request          판매글 생성 요청 정보
     * @return 생성되거나 조회된 판매글 정보
     */
    @Operation(
            summary = "추천으로부터 판매글 생성",
            description = """
                    ACCEPTED 상태의 판매 추천으로부터 판매글을 생성합니다.
                    중복 생성을 방지하며, 이미 판매글이 존재하는 경우 기존 판매글을 반환합니다.
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "판매글 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "추천이 ACCEPTED 상태가 아니거나 판매 타입이 아님"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "추천을 찾을 수 없음"),
                    @ApiResponse(responseCode = "409", description = "해당 추천에 대한 판매글이 이미 존재"),
                    @ApiResponse(responseCode = "500", description = "서버 로직 오류")
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
     * 배치 실행 이력 조회
     * 추천 배치 스케줄러의 실행 이력을 조회합니다.
     * 성공/실패 여부, 처리된 사용자 수, 생성된 추천 수, 실행 시간 등을 확인할 수 있습니다.
     *
     * @param page      페이지 번호 (0부터 시작, 기본값 0)
     * @param size      페이지 크기 (기본값 20)
     * @param sort      정렬 기준 필드 (기본값 startTime)
     * @param direction 정렬 방향 (ASC/DESC, 기본값 DESC)
     * @return 배치 이력 목록
     */
    @Operation(
            summary = "배치 실행 이력 조회",
            description = """
                    추천 배치 스케줄러의 실행 이력을 조회합니다.
                    성공/실패 여부, 처리된 사용자 수, 생성된 추천 수, 실행 시간 등을 확인할 수 있습니다.
                    기본적으로 시작 시간 기준 최신순으로 정렬됩니다.
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "배치 이력 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "500", description = "서버 로직 오류")
            }
    )
    @GetMapping("/batch-history")
    public ResponseEntity<Response<RecommendationBatchHistoryListResponse>> getBatchHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sort)
        );

        Page<RecommendationBatchHistory> batchHistoryPage = batchHistoryQueryService.getRecentBatchHistory(pageable);

        Page<RecommendationBatchHistoryResponse> responsePage = batchHistoryPage
                .map(RecommendationBatchHistoryResponse::from);

        RecommendationBatchHistoryListResponse response = RecommendationBatchHistoryListResponse.from(responsePage);

        return Response.success(response, RecommendationSuccessCode.BATCH_HISTORY_GET_OK);
    }

    /**
     * 최근 배치 실행 이력 조회
     * 가장 최근에 실행된 배치의 상세 정보를 조회
     *
     * @return 최근 배치 이력
     */
    @Operation(
            summary = "최근 배치 실행 이력 조회",
            description = "가장 최근에 실행된 배치의 상세 정보를 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "배치 이력 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "배치 이력이 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 로직 오류")
            }
    )
    @GetMapping("/batch-history/latest")
    public ResponseEntity<Response<RecommendationBatchHistoryResponse>> getLatestBatchHistory() {
        RecommendationBatchHistory batchHistory = batchHistoryQueryService.getLastBatchHistory();
        RecommendationBatchHistoryResponse response = RecommendationBatchHistoryResponse.from(batchHistory);

        return Response.success(response, RecommendationSuccessCode.BATCH_HISTORY_GET_OK);
    }

    /**
     * 기부 추천 수락 후 주변 기부처 검색
     * ACCEPTED 상태의 기부 추천(DONATION)에서 사용자 위치 기반으로 주변 기부처를 검색합니다.
     * 사용자의 거래 위치(tradeLocation)를 기준으로 검색합니다.
     *
     * @param recommendationId 추천 ID
     * @param authUser         인증된 사용자 정보
     * @param radius           검색 반경 (미터, 기본값: 5000m = 5km)
     * @param keyword          검색 키워드 (선택사항)
     * @return 거리순으로 정렬된 기부처 목록
     */
    @Operation(
            summary = "기부 추천에서 주변 기부처 검색",
            description = """
                    ACCEPTED 상태의 기부 추천에서 사용자 위치 기반으로 주변 기부처를 검색합니다.
                    
                    사용자의 거래 위치를 기준으로 주변 기부처를 찾습니다.
                    검색 결과는 거리순으로 정렬됩니다.
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "기부처 검색 성공"),
                    @ApiResponse(responseCode = "400", description = "추천이 ACCEPTED 상태가 아니거나 기부 타입이 아님"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "추천을 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 로직 오류")
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