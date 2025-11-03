package org.example.ootoutfitoftoday.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.SliceResponse;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.example.ootoutfitoftoday.domain.chat.exception.ChatSuccessCode;
import org.example.ootoutfitoftoday.domain.chat.service.query.ChatQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "채팅", description = "채팅 관련 API")
@RestController
@RequestMapping("/v1/chatrooms/{chatroomId}/chats")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatQueryService chatQueryService;


    /**
     * 채팅 리스트 조회
     *
     * @param chatroomId 채팅방 id
     * @param page       페이지
     * @param size       크기
     * @return 채팅 리스트
     */
    @Operation(
            summary = "채팅 조회",
            description = "회원이 채팅을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping
    public ResponseEntity<SliceResponse<ChatResponse>> getChats(
            @PathVariable Long chatroomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Slice<ChatResponse> chatResponses = chatQueryService.getChats(chatroomId, pageable);

        return SliceResponse.success(chatResponses, ChatSuccessCode.RETRIEVED_CHATS);
    }
}
