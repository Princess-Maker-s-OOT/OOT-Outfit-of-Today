package org.example.ootoutfitoftoday.domain.closet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiPageResponse;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetSaveRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetMyResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetPublicResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetSaveResponse;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetSuccessCode;
import org.example.ootoutfitoftoday.domain.closet.service.command.ClosetCommandService;
import org.example.ootoutfitoftoday.domain.closet.service.query.ClosetQueryServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/closets")
public class ClosetController {

    private final ClosetCommandService closetCommandService;
    private final ClosetQueryServiceImpl closetQueryService;

    // 옷장 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ClosetSaveResponse>> createCloset(
            @Valid @RequestBody ClosetSaveRequest closetSaveRequest
    ) {
        ClosetSaveResponse closetSaveResponse = closetCommandService.createCloset(closetSaveRequest);

        return ApiResponse.success(closetSaveResponse, ClosetSuccessCode.CLOSET_CREATED);
    }

    // 공개 옷장 리스트 조회
    @GetMapping("/public")
    public ResponseEntity<ApiPageResponse<ClosetGetPublicResponse>> getPublicClosets(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt") String sort, @RequestParam(defaultValue = "DESC") String direction) {

        Page<ClosetGetPublicResponse> closetGetPublicResponses = closetQueryService.getPublicClosets(page, size, sort, direction);

        return ApiPageResponse.success(closetGetPublicResponses, ClosetSuccessCode.CLOSET_GET_PUBLIC_OK);
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
     * @param authUser
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

        return ApiPageResponse.success(closetGetMyResponses, ClosetSuccessCode.CLOSET_GET_MY_OK);
    }
}
