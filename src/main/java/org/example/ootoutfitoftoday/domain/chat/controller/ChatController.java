package org.example.ootoutfitoftoday.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.ApiSliceResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.example.ootoutfitoftoday.domain.chat.exception.ChatSuccessCode;
import org.example.ootoutfitoftoday.domain.chat.service.query.ChatQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/chatrooms/{chatroomId}/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatQueryService chatQueryService;

    /**
     * @param chatroomId 채팅방 Id
     * @param authUser   토큰 정보
     * @return 채팅 리스트
     */
    @GetMapping
    public ResponseEntity<ApiSliceResponse<ChatResponse>> getChats(
            @PathVariable Long chatroomId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Long userId = authUser.getUserId();

        Slice<ChatResponse> chatResponses = chatQueryService.getChats(chatroomId, userId, pageable);

        return ApiSliceResponse.success(chatResponses, ChatSuccessCode.RETRIEVED_CHATS);
    }
}
