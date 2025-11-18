package org.example.ootoutfitoftoday.domain.closet.controller;

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
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetCreateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetUpdateRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.*;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetSuccessCode;
import org.example.ootoutfitoftoday.domain.closet.service.command.ClosetCommandService;
import org.example.ootoutfitoftoday.domain.closet.service.query.ClosetQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "옷장 관리", description = "옷장관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/closets")
public class ClosetController {

    private final ClosetCommandService closetCommandService;
    private final ClosetQueryService closetQueryService;

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
            @Valid @RequestBody ClosetCreateRequest closetCreateRequest
    ) {
        log.info("옷장 생성 요청 - 사용자: {}, 이름: {}", authUser.getUserId(), closetCreateRequest.name());
        log.debug("옷장 생성 상세 - 공개여부: {}, 이미지ID: {}",
                closetCreateRequest.isPublic(), closetCreateRequest.imageId());

        ClosetCreateResponse closetCreateResponse = closetCommandService.createCloset(
                authUser.getUserId(),
                closetCreateRequest
        );

        log.info("옷장 생성 완료 - 옷장ID: {}, 사용자: {}",
                closetCreateResponse.closetId(), authUser.getUserId());

        return Response.success(closetCreateResponse, ClosetSuccessCode.CLOSET_CREATED);
    }

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
        log.info("공개 옷장 목록 조회 요청 - 페이지: {}, 크기: {}, 정렬: {}, 방향: {}", page, size, sort, direction);

        Page<ClosetGetPublicResponse> closetGetPublicResponses = closetQueryService.getPublicClosets(
                page,
                size,
                sort,
                direction
        );

        log.info("공개 옷장 목록 조회 완료 - 조회 건수: {}, 전체 건수: {}, 전체 페이지: {}",
                closetGetPublicResponses.getContent().size(),
                closetGetPublicResponses.getTotalElements(),
                closetGetPublicResponses.getTotalPages());

        return PageResponse.success(closetGetPublicResponses, ClosetSuccessCode.CLOSETS_GET_PUBLIC_OK);
    }

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
        log.info("옷장 상세 조회 요청 - 옷장ID: {}", closetId);

        ClosetGetResponse closetGetResponse = closetQueryService.getCloset(closetId);

        log.debug("옷장 조회 완료 - 옷장ID: {}, 이름: {}, 공개여부: {}",
                closetId, closetGetResponse.name(), closetGetResponse.isPublic());

        return Response.success(closetGetResponse, ClosetSuccessCode.CLOSET_GET_OK);
    }

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
        log.info("내 옷장 목록 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}, 정렬: {}, 방향: {}",
                authUser.getUserId(), page, size, sort, direction);

        Page<ClosetGetMyResponse> closetGetMyResponses = closetQueryService.getMyClosets(
                authUser.getUserId(),
                page,
                size,
                sort,
                direction
        );

        log.info("내 옷장 목록 조회 완료 - 조회 건수: {}, 사용자: {}, 전체 건수: {}",
                closetGetMyResponses.getContent().size(), authUser.getUserId(),
                closetGetMyResponses.getTotalElements());

        return PageResponse.success(closetGetMyResponses, ClosetSuccessCode.CLOSETS_GET_MY_OK);
    }

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
        log.info("옷장 수정 요청 - 옷장ID: {}, 사용자: {}", closetId, authUser.getUserId());
        log.debug("수정 요청 상세 - 이름: {}, 공개여부: {}, 이미지ID: {}",
                closetUpdateRequest.name(), closetUpdateRequest.isPublic(), closetUpdateRequest.imageId());

        ClosetUpdateResponse closetUpdateResponse = closetCommandService.updateCloset(
                authUser.getUserId(),
                closetId,
                closetUpdateRequest
        );

        log.info("옷장 수정 완료 - 옷장ID: {}", closetId);

        return Response.success(closetUpdateResponse, ClosetSuccessCode.CLOSET_UPDATE_OK);
    }

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
        log.info("옷장 삭제 요청 - 옷장ID: {}, 사용자: {}", closetId, authUser.getUserId());

        ClosetDeleteResponse response = closetCommandService.deleteCloset(
                authUser.getUserId(),
                closetId
        );

        log.info("옷장 삭제 완료 - 옷장ID: {}, 삭제시간: {}",
                response.closetId(), response.deletedAt());

        return Response.success(response, ClosetSuccessCode.CLOSET_DELETE_OK);
    }
}