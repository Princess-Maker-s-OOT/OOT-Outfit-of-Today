package org.example.ootoutfitoftoday.domain.wearrecord.controller;

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
import org.example.ootoutfitoftoday.domain.wearrecord.dto.request.WearRecordCreateRequest;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordCreateResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordGetMyResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.exception.WearRecordSuccessCode;
import org.example.ootoutfitoftoday.domain.wearrecord.service.command.WearRecordCommandService;
import org.example.ootoutfitoftoday.domain.wearrecord.service.query.WearRecordQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "착용 기록 관리", description = "옷 착용 기록 및 이력 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/wear-records")
public class WearRecordController {

    private final WearRecordCommandService wearRecordCommandService;
    private final WearRecordQueryService wearRecordQueryService;

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
        log.info("착용 기록 등록 요청 시작 - 사용자 ID: {}, 옷 ID: {}", authUser.getUserId(), request.clothesId());

        WearRecordCreateResponse response = wearRecordCommandService.createWearRecord(
                authUser.getUserId(),
                request
        );

        log.info("착용 기록 등록 완료 - 착용 기록 ID: {}", response.wearRecordId());

        return Response.success(response, WearRecordSuccessCode.WEAR_RECORD_CREATED);
    }

    @Operation(
            summary = "내 착용 기록 리스트 조회",
            description = "로그인한 사용자의 전체 착용 기록을 최신순(wornAt DESC)으로 페이징하여 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping
    public ResponseEntity<PageResponse<WearRecordGetMyResponse>> getMyWearRecords(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "wornAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        log.info("내 착용 기록 리스트 조회 요청 - 사용자 ID: {}, 페이지: {}, 사이즈: {}, 정렬: {} {}",
                authUser.getUserId(), page, size, sort, direction);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sort)
        );

        Page<WearRecordGetMyResponse> responsePage = wearRecordQueryService.getMyWearRecords(
                authUser.getUserId(),
                pageable
        );

        log.info("내 착용 기록 리스트 조회 완료 - 총 {}건 중 {}건 반환",
                responsePage.getTotalElements(), responsePage.getNumberOfElements());

        return PageResponse.success(responsePage, WearRecordSuccessCode.WEAR_RECORDS_GET_OK);
    }
}