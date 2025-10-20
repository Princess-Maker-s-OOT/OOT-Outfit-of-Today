package org.example.ootoutfitoftoday.domain.closet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiPageResponse;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetSaveRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetUpdateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.*;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetSuccessCode;
import org.example.ootoutfitoftoday.domain.closet.service.command.ClosetCommandServiceImpl;
import org.example.ootoutfitoftoday.domain.closet.service.query.ClosetQueryServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/closets")
public class ClosetController {

    private final ClosetCommandServiceImpl closetCommandService;
    private final ClosetQueryServiceImpl closetQueryService;

    /**
     * 옷장 등록
     *
     * @param closetSaveRequest: 옷장 등록 요청 객체 (이름, 설명 등 포함)
     * @return ClosetSaveResponse: 등록된 옷장 정보와 성공 응답 코드
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ClosetSaveResponse>> createCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ClosetSaveRequest closetSaveRequest
    ) {
        ClosetSaveResponse closetSaveResponse = closetCommandService.createCloset(
                authUser.getUserId(),
                closetSaveRequest
        );

        return ApiResponse.success(closetSaveResponse, ClosetSuccessCode.CLOSET_CREATED);
    }

    /**
     * 공개 옷장 리스트 조회
     *
     * @param page:      페이지 번호
     * @param size:      페이지 크기
     * @param sort:      정렬 기준
     * @param direction: 정렬 방향
     * @return Page<ClosetGetPublicResponse>: 공개 옷장 리스트
     */
    @GetMapping("/public")
    public ResponseEntity<ApiPageResponse<ClosetGetPublicResponse>> getPublicClosets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {

        Page<ClosetGetPublicResponse> closetGetPublicResponses = closetQueryService.getPublicClosets(
                page,
                size,
                sort,
                direction
        );

        return ApiPageResponse.success(closetGetPublicResponses, ClosetSuccessCode.CLOSETS_GET_PUBLIC_OK);
    }

    /**
     * 옷장 상세 조회
     *
     * @param closetId: 조회할 옷장의 ID
     * @return ClosetGetResponse: 옷장 상세 정보 DTO
     * @throws ClosetException: 옷장이 존재하지 않거나 삭제된 경우 (CLOSET_NOT_FOUND)
     */
    @GetMapping("/{closetId}")
    public ResponseEntity<ApiResponse<ClosetGetResponse>> getCloset(
            @PathVariable Long closetId
    ) {
        ClosetGetResponse closetGetResponse = closetQueryService.getCloset(closetId);

        return ApiResponse.success(closetGetResponse, ClosetSuccessCode.CLOSET_GET_OK);
    }

    /**
     * 내 옷장 리스트 조회
     *
     * @param authUser:  인증된 사용자 정보
     * @param page:      페이지 번호
     * @param size:      페이지 크기
     * @param sort:      정렬 기준
     * @param direction: 정렬 방향
     * @return Page<ClosetGetMyResponse>: 내 옷장 리스트
     */
    @GetMapping("/me")
    public ResponseEntity<ApiPageResponse<ClosetGetMyResponse>> getClosetByMe(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {

        Page<ClosetGetMyResponse> closetGetMyResponses = closetQueryService.getMyClosets(
                authUser.getUserId(),
                page,
                size,
                sort,
                direction
        );

        return ApiPageResponse.success(closetGetMyResponses, ClosetSuccessCode.CLOSETS_GET_MY_OK);
    }

    /**
     * 옷장 정보 수정
     *
     * @param authUser:            인증된 사용자 정보
     * @param closetId:            조회할 옷장의 ID
     * @param closetUpdateRequest: 옷장 수정 요청 객체 (이름, 설명 등 포함)
     * @return closetUpdateResponse: 수정된 옷장 정보와 성공 응답 코드
     */
    @PutMapping("/{closetId}")
    public ResponseEntity<ApiResponse<ClosetUpdateResponse>> updateCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId,
            @Valid @RequestBody ClosetUpdateRequest closetUpdateRequest
    ) {

        ClosetUpdateResponse closetUpdateResponse = closetCommandService.updateCloset(
                authUser.getUserId(),
                closetId,
                closetUpdateRequest
        );

        return ApiResponse.success(closetUpdateResponse, ClosetSuccessCode.CLOSET_UPDATE_OK);
    }
}
