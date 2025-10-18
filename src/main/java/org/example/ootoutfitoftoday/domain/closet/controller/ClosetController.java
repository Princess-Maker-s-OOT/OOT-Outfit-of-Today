package org.example.ootoutfitoftoday.domain.closet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetSaveRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetSaveResponse;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetSuccessCode;
import org.example.ootoutfitoftoday.domain.closet.service.command.ClosetCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/closets")
public class ClosetController {

    private final ClosetCommandService closetCommandService;

    // 옷장 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ClosetSaveResponse>> createCloset(
            @Valid @RequestBody ClosetSaveRequest closetSaveRequest
    ) {
        ClosetSaveResponse closetSaveResponse = closetCommandService.createCloset(closetSaveRequest);

        return ApiResponse.success(closetSaveResponse, ClosetSuccessCode.CLOSET_CREATED);
    }
}
