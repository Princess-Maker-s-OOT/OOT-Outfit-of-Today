package org.example.ootoutfitoftoday.domain.chatroom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.common.response.SliceResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.chatroom.dto.request.ChatroomRequest;
import org.example.ootoutfitoftoday.domain.chatroom.dto.response.ChatroomResponse;
import org.example.ootoutfitoftoday.domain.chatroom.exception.ChatroomSuccessCode;
import org.example.ootoutfitoftoday.domain.chatroom.service.command.ChatroomCommandService;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "채팅방", description = "채팅방 API")
@RestController
@RequestMapping("/v1/chatrooms")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ChatroomController {

    private final ChatroomCommandService chatroomCommandService;
    private final ChatroomQueryService chatroomQueryService;

    /**
     * 채팅방 생성 API
     *
     * @param chatroomRequest 게시물의 id 정보
     * @param authUser        토큰 정보
     * @return 공통 응답만 반환
     */
    @Operation(
            summary = "채팅방 생성",
            description = "회원이 채팅방을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "409", description = "이미 채팅방이 존재함")
            }
    )
    @PostMapping
    public ResponseEntity<Response<Void>> createChatroom(
            @RequestBody ChatroomRequest chatroomRequest,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        Long userId = authUser.getUserId();

        chatroomCommandService.createChatroom(chatroomRequest, userId);

        return Response.success(null, ChatroomSuccessCode.CREATED_CHATROOM);
    }

    /**
     * 채팅방 조회 API
     *
     * @param authUser 토근 정보
     * @param page     페이지
     * @param size     크기
     * @return 채팅방 리스트
     */
    @Operation(
            summary = "채팅방 조회",
            description = "회원이 채팅방을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 페이지 또는 사이즈 파라미터"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping
    public ResponseEntity<SliceResponse<ChatroomResponse>> getChatrooms(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = authUser.getUserId();

        Pageable pageable = PageRequest.of(page, size);

        Slice<ChatroomResponse> chatroomResponses = chatroomQueryService.getChatrooms(userId, pageable);

        return SliceResponse.success(chatroomResponses, ChatroomSuccessCode.RETRIEVED_CHATROOMS);
    }

    /**
     * 채팅방 삭제 API
     *
     * @param authUser   토큰 정보
     * @param chatroomId 채팅방 아이디
     * @return 공통 응답만 반환
     */
    @Operation(
            summary = "채팅방 삭제",
            description = "회원이 채팅방을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
            }
    )
    @DeleteMapping("/{chatroomId}")
    public ResponseEntity<Response<Void>> deleteChatroom(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long chatroomId
    ) {
        Long userId = authUser.getUserId();
        chatroomCommandService.deleteChatroom(chatroomId, userId);

        return Response.success(null, ChatroomSuccessCode.DELETED_CHATROOM);
    }
}
