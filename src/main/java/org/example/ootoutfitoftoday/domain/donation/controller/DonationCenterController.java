package org.example.ootoutfitoftoday.domain.donation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;
import org.example.ootoutfitoftoday.domain.donation.exception.DonationSuccessCode;
import org.example.ootoutfitoftoday.domain.donation.service.query.DonationCenterQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 기부처 검색 컨트롤러 (Public API - 인증 불필요)
 */
@Tag(name = "기부처 검색", description = "주변 기부처 검색 관련 API (비회원 접근 가능)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/donation-centers")
public class DonationCenterController {

    private final DonationCenterQueryService donationCenterQueryService;

    /**
     * 주변 기부처 검색
     *
     * @param latitude  사용자 위치의 위도 (필수)
     * @param longitude 사용자 위치의 경도 (필수)
     * @param radius    검색 반경 (미터, 기본값: 5000m = 5km)
     * @param keyword   검색 키워드 (선택사항, 없으면 기본 키워드로 검색: 의류기부, 헌옷수거함, 아름다운가게, 의류수거함)
     * @return 거리순으로 정렬된 기부처 목록
     */
    @Operation(
            summary = "주변 기부처 검색",
            description = "사용자 위치 기반으로 주변 기부처를 검색합니다. " +
                    "비회원도 접근 가능합니다. " +
                    "검색 결과는 거리순으로 정렬되며, 자동으로 DB에 저장됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "검색 성공",
                            content = @Content(schema = @Schema(implementation = DonationCenterSearchResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (유효하지 않은 좌표값)"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "카카오맵 API 호출 실패"
                    )
            }
    )
    @GetMapping("/search")
    public ResponseEntity<Response<List<DonationCenterSearchResponse>>> searchNearbyDonationCenters(
            @Parameter(
                    description = "위도 (예: 37.5665)",
                    required = true,
                    example = "37.5665"
            )
            @RequestParam Double latitude,

            @Parameter(
                    description = "경도 (예: 126.9780)",
                    required = true,
                    example = "126.9780"
            )
            @RequestParam Double longitude,

            @Parameter(
                    description = "검색 반경 (미터 단위, 기본값: 5000m = 5km)",
                    example = "5000"
            )
            @RequestParam(required = false) Integer radius,

            @Parameter(
                    description = "검색 키워드 (선택사항, 없으면 기본 키워드로 검색)",
                    example = "의류기부"
            )
            @RequestParam(required = false) String keyword
    ) {

        List<DonationCenterSearchResponse> donationCenters = donationCenterQueryService.searchNearbyDonationCenters(
                latitude,
                longitude,
                radius,
                keyword
        );

        return Response.success(donationCenters, DonationSuccessCode.DONATION_CENTER_SEARCH_SUCCESS);
    }
}
