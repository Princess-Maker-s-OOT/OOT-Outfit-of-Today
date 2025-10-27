package org.example.ootoutfitoftoday.domain.closetclotheslink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "옷장-옷 관리", description = "옷장에 옷을 등록, 조회, 삭제하는 API")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "옷장에 옷 등록",
            description = "선택한 옷장에 이미 등록된 옷을 추가합니다. 같은 옷을 중복으로 등록할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "이미 등록된 옷 또는 잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "해당 옷장에 대한 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "옷장 또는 옷을 찾을 수 없음")
            }
    )
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
     * 특정 옷장에 등록된 옷 리스트 조회
     *
     * @param authUser  인증된 사용자 정보
     * @param closetId  조회할 옷장 ID
     * @param page      페이지 번호 (기본값: 0)
     * @param size      페이지 크기 (기본값: 10)
     * @param sort      정렬 기준 (기본값: createdAt)
     * @param direction 정렬 방향 (기본값: DESC)
     * @return PageResponse<ClosetClothesLinkGetResponse> 옷 목록
     */
    @Operation(
            summary = "옷장에 등록된 옷 리스트 조회",
            description = "선택한 옷장에 등록된 모든 옷을 조회합니다. 최근 등록순으로 정렬됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "해당 옷장에 대한 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "옷장을 찾을 수 없음")
            }
    )
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


    /**
     * 옷장에서 옷 제거
     *
     * @param authUser  인증된 사용자 정보
     * @param closetId  옷을 제거할 옷장 ID
     * @param clothesId 제거할 옷 ID
     * @return ClosetClothesLinkDeleteResponse 제거된 연결 정보
     */
    @Operation(
            summary = "옷장에서 옷 제거",
            description = "선택한 옷장에 등록된 옷을 제거합니다. 실제 옷 데이터는 삭제되지 않고 연결만 해제됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "연결되지 않은 옷"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "해당 옷장에 대한 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "옷장을 찾을 수 없음")
            }
    )
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