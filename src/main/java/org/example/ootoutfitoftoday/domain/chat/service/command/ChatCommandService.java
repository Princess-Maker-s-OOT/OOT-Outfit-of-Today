package org.example.ootoutfitoftoday.domain.chat.service.command;

import org.example.ootoutfitoftoday.domain.chat.dto.request.ChatRequest;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatCreateResponse;

public interface ChatCommandService {

    ChatCreateResponse createChat(ChatRequest chatRequest, Long chatroomId, Long userId);
}
