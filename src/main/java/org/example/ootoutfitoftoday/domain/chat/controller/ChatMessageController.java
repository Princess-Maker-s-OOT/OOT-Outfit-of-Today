package org.example.ootoutfitoftoday.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.chat.config.CustomUserDetails;
import org.example.ootoutfitoftoday.domain.chat.dto.request.ChatRequest;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.example.ootoutfitoftoday.domain.chat.service.command.ChatCommandService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatCommandService chatCommandService;

    /**
     * @param chatRequest 채팅 요청 데이터
     * @param chatroomId  채팅방 Id
     * @param userDetails 토큰 정보
     * @return 채팅 응답 데이터
     */
    @MessageMapping("/chat/message/{chatroomNo}")
    @SendTo("/topic/chat/{chatroomNo}")
    public ChatResponse sendAndSaveMessage(
            @Payload ChatRequest chatRequest,
            @DestinationVariable(value = "chatroomNo") Long chatroomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("ChatController.sendAndSaveMessage");
        Long userId = userDetails.getUserId();

        return chatCommandService.createChat(chatRequest, chatroomId, userId);
    }
}
