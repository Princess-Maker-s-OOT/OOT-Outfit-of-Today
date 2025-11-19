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

    @MessageMapping("/chat/message/{chatroomNo}")
    @SendTo("/topic/chat/{chatroomNo}")
    public ChatResponse sendAndSaveMessage(
            @Payload ChatRequest chatRequest,
            @DestinationVariable(value = "chatroomNo") Long chatroomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        log.info("ChatMessageController.sendAndSaveMessage: chatroomId={} 채팅 동작", chatroomId);

        Long userId = userDetails.getUserId();

        return chatCommandService.createChat(chatRequest, chatroomId, userId);
    }
}
