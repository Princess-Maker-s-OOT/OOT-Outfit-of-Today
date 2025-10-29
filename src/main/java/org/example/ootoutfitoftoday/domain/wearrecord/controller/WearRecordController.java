package org.example.ootoutfitoftoday.domain.wearrecord.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.request.WearRecordCreateRequest;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordCreateResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.exception.WearRecordSuccessCode;
import org.example.ootoutfitoftoday.domain.wearrecord.service.command.WearRecordCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "착용 기록 관리", description = "옷 착용 기록 및 이력 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/wear-records")
public class WearRecordController {

    private final WearRecordCommandService wearRecordCommandService;

    /**
     * 착용 기록 등록
     *
     * @param authUser: 인증된 사용자 정보
     * @param request:  착용 기록 등록 요청 객체 (clothesId 포함)
     * @return WearRecordCreateResponse: 등록된 기록 ID와 성공 응답 코드
     */
    @Operation(
            summary = "착용 기록 등록",
            description = "사용자가 특정 옷을 착용했음을 기록하고, 해당 옷의 마지막 착용 일시를 업데이트합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (clothesId 누락 등)"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 옷에 기록 시도)"),
                    @ApiResponse(responseCode = "404", description = "옷을 찾을 수 없음 (ID 오류 또는 삭제된 옷)")
            }
    )
    @PostMapping
    public ResponseEntity<Response<WearRecordCreateResponse>> createWearRecord(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody WearRecordCreateRequest request
    ) {
        WearRecordCreateResponse response = wearRecordCommandService.createWearRecord(
                authUser.getUserId(),
                request
        );

        return Response.success(response, WearRecordSuccessCode.WEAR_RECORD_CREATED);
    }
}