package org.example.ootoutfitoftoday.domain.chat.controller;

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

@RestController
@RequestMapping("/v1/chatrooms/{chatroomId}/chats")
@RequiredArgsConstructor
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
