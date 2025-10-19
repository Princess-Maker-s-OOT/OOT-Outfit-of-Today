package org.example.ootoutfitoftoday.domain.closet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiPageResponse;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetSaveRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetPublicResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetSaveResponse;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetSuccessCode;
import org.example.ootoutfitoftoday.domain.closet.service.command.ClosetCommandService;
import org.example.ootoutfitoftoday.domain.closet.service.query.ClosetQueryServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
}
