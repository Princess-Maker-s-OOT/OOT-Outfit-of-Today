package org.example.ootoutfitoftoday.domain.closetclotheslink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.request.ClosetClothesLinkRequest;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.exception.ClosetClothesLinkSuccessCode;
import org.example.ootoutfitoftoday.domain.closetclotheslink.service.command.ClosetClothesLinkCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/closets/{closetId}/clothes")
public class ClosetClothesLinkController {

    private final ClosetClothesLinkCommandService closetClothesLinkCommandService;

    // 해당 옷장에 옷 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ClosetClothesLinkResponse>> createClosetClothesLink(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId,
            @Valid @RequestBody ClosetClothesLinkRequest closetClothesLinkRequest
    ) {

        ClosetClothesLinkResponse closetClothesLinkResponse = closetClothesLinkCommandService.createClosetClothesLink(
                authUser.getUserId(),
                closetId,
                closetClothesLinkRequest
        );

        return ApiResponse.success(closetClothesLinkResponse, ClosetClothesLinkSuccessCode.CLOSET_CLOTHES_LINKED);
    }
}
