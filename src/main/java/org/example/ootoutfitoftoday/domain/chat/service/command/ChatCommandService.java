package org.example.ootoutfitoftoday.domain.chat.service.command;

import org.example.ootoutfitoftoday.domain.chat.dto.request.ChatRequest;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;

public interface ChatCommandService {

    ChatResponse createChat(ChatRequest chatRequest, Long chatroomId, Long userId);
}
