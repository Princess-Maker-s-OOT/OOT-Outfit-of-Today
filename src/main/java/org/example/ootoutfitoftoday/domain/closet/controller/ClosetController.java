package org.example.ootoutfitoftoday.domain.closet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
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

@Tag(name = "옷장 관리", description = "옷장관련 API")
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
    @Operation(
            summary = "옷장 등록",
            description = "회원이 자신의 옷장을 등록합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @PostMapping
    public ResponseEntity<Response<ClosetCreateResponse>> createCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ClosetSaveRequest closetSaveRequest
    ) {
        ClosetCreateResponse closetCreateResponse = closetCommandService.createCloset(
                authUser.getUserId(),
                closetSaveRequest
        );

        return Response.success(closetCreateResponse, ClosetSuccessCode.CLOSET_CREATED);
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
    @Operation(
            summary = "공개 옷장 전체 조회",
            description = "공개 옷장 전체를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @GetMapping("/public")
    public ResponseEntity<PageResponse<ClosetGetPublicResponse>> getPublicClosets(
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

        return PageResponse.success(closetGetPublicResponses, ClosetSuccessCode.CLOSETS_GET_PUBLIC_OK);
    }

    /**
     * 옷장 상세 조회
     *
     * @param closetId: 조회할 옷장의 ID
     * @return ClosetGetResponse: 옷장 상세 정보 DTO
     * @throws ClosetException: 옷장이 존재하지 않거나 삭제된 경우 (CLOSET_NOT_FOUND)
     */
    @Operation(
            summary = "옷장 상세 조회",
            description = "회원이 옷장의 상세 정보를 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "옷장을 찾을 수 없음")
            }
    )
    @GetMapping("/{closetId}")
    public ResponseEntity<Response<ClosetGetResponse>> getCloset(
            @PathVariable Long closetId
    ) {
        ClosetGetResponse closetGetResponse = closetQueryService.getCloset(closetId);

        return Response.success(closetGetResponse, ClosetSuccessCode.CLOSET_GET_OK);
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
    @Operation(
            summary = "내 옷장 전체 조회",
            description = "회원이 자신의 전체 옷장을 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<PageResponse<ClosetGetMyResponse>> getClosetByMe(
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

        return PageResponse.success(closetGetMyResponses, ClosetSuccessCode.CLOSETS_GET_MY_OK);
    }

    /**
     * 옷장 정보 수정
     *
     * @param authUser:            인증된 사용자 정보
     * @param closetId:            조회할 옷장의 ID
     * @param closetUpdateRequest: 옷장 수정 요청 객체 (이름, 설명 등 포함)
     * @return closetUpdateResponse: 수정된 옷장 정보와 성공 응답 코드
     */
    @Operation(
            summary = "내 옷장 정보 수정",
            description = "회원이 자신의 옷장 정보를 수정합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "옷장을 찾을 수 없음")
            }
    )
    @PutMapping("/{closetId}")
    public ResponseEntity<Response<ClosetUpdateResponse>> updateCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId,
            @Valid @RequestBody ClosetUpdateRequest closetUpdateRequest
    ) {

        ClosetUpdateResponse closetUpdateResponse = closetCommandService.updateCloset(
                authUser.getUserId(),
                closetId,
                closetUpdateRequest
        );

        return Response.success(closetUpdateResponse, ClosetSuccessCode.CLOSET_UPDATE_OK);
    }

    /**
     * 옷장 삭제
     *
     * @param authUser: 인증된 사용자 정보
     * @param closetId: 삭제할 옷장의 ID
     * @return ClosetDeleteResponse 삭제된 옷장 ID와 삭제 시간
     */
    @Operation(
            summary = "내 옷장 삭제",
            description = "회원이 자신의 옷장을 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "옷장을 찾을 수 없음")
            }
    )
    @DeleteMapping("/{closetId}")
    public ResponseEntity<Response<ClosetDeleteResponse>> deleteCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId
    ) {

        ClosetDeleteResponse response = closetCommandService.deleteCloset(
                authUser.getUserId(),
                closetId
        );

        return Response.success(response, ClosetSuccessCode.CLOSET_DELETE_OK);
    }
}