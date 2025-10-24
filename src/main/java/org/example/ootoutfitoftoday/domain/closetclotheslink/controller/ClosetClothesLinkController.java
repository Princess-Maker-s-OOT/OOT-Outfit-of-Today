package org.example.ootoutfitoftoday.domain.closetclotheslink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.request.ClosetClothesLinkRequest;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkDeleteResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkGetResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.exception.ClosetClothesLinkSuccessCode;
import org.example.ootoutfitoftoday.domain.closetclotheslink.service.command.ClosetClothesLinkCommandService;
import org.example.ootoutfitoftoday.domain.closetclotheslink.service.query.ClosetClothesLinkQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/closets/{closetId}/clothes")
public class ClosetClothesLinkController {

    private final ClosetClothesLinkCommandService closetClothesLinkCommandService;
    private final ClosetClothesLinkQueryService closetClothesLinkQueryService;

    /**
     * 특정 옷장에 옷 등록
     *
     * @param authUser                 인증된 사용자 정보
     * @param closetId                 옷을 등록할 옷장 ID
     * @param closetClothesLinkRequest 등록할 옷 ID
     * @return ClosetClothesLinkResponse 연결 정보
     */
    @PostMapping
    public ResponseEntity<Response<ClosetClothesLinkResponse>> createClosetClothesLink(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId,
            @Valid @RequestBody ClosetClothesLinkRequest closetClothesLinkRequest
    ) {

        ClosetClothesLinkResponse closetClothesLinkResponse = closetClothesLinkCommandService.createClosetClothesLink(
                authUser.getUserId(),
                closetId,
                closetClothesLinkRequest
        );

        return Response.success(closetClothesLinkResponse, ClosetClothesLinkSuccessCode.CLOSET_CLOTHES_LINKED);
    }

    /**
     * 특정 옷장에 등록된 옷 목록 조회
     *
     * @param authUser  인증된 사용자 정보
     * @param closetId  조회할 옷장 ID
     * @param page      페이지 번호 (기본값: 0)
     * @param size      페이지 크기 (기본값: 10)
     * @param sort      정렬 기준 (기본값: createdAt)
     * @param direction 정렬 방향 (기본값: DESC)
     * @return ApiPageResponse<ClothesInClosetResponse> 옷 목록
     */
    @GetMapping
    public ResponseEntity<PageResponse<ClosetClothesLinkGetResponse>> getClosetClothesLink(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {

        Page<ClosetClothesLinkGetResponse> closetClothesLinkGetResponses = closetClothesLinkQueryService.getClothesInCloset(
                authUser.getUserId(),
                closetId,
                page,
                size,
                sort,
                direction
        );

        return PageResponse.success(closetClothesLinkGetResponses, ClosetClothesLinkSuccessCode.CLOSET_CLOTHES_LIST_OK);
    }

    // 옷장에서 옷 삭제
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Response<ClosetClothesLinkDeleteResponse>> deleteClosetClothesLink(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId,
            @PathVariable Long clothesId
    ) {

        ClosetClothesLinkDeleteResponse closetClothesLinkDeleteResponse = closetClothesLinkCommandService.deleteClosetClothesLink(
                authUser.getUserId(),
                closetId,
                clothesId
        );

        return Response.success(closetClothesLinkDeleteResponse, ClosetClothesLinkSuccessCode.CLOSET_CLOTHES_DELETED);
    }
}