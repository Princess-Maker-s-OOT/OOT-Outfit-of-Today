package org.example.ootoutfitoftoday.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatResponse {

    private final Long chatId;
    private final Long chatroomId;
    private final Long userId;
    private final String content;
    private final LocalDateTime createdAt;

    @Builder
    private ChatResponse(
            Long chatId,
            Long chatroomId,
            Long userId,
            String content,
            LocalDateTime createdAt
    ) {
        this.chatId = chatId;
        this.chatroomId = chatroomId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ChatResponse of(
            Long chatId,
            Long chatroomId,
            Long userId,
            String content,
            LocalDateTime createdAt
    ) {

        return ChatResponse.builder()
                .chatId(chatId)
                .chatroomId(chatroomId)
                .userId(userId)
                .content(content)
                .createdAt(createdAt)
                .build();
    }
}
