package org.example.ootoutfitoftoday.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatResponse {

    private final Long chatroomId;
    private final Long userId;
    private final String userNickname;
    private final Long chatId;
    private final String content;
    private final LocalDateTime createdAt;

    @Builder
    private ChatResponse(
            Long chatroomId,
            Long userId,
            String userNickname,
            Long chatId,
            String content,
            LocalDateTime createdAt
    ) {
        this.chatroomId = chatroomId;
        this.userId = userId;
        this.userNickname = userNickname;
        this.chatId = chatId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ChatResponse of(
            Long chatroomId,
            Long userId,
            String userNickname,
            Long chatId,
            String content,
            LocalDateTime createdAt
    ) {

        return ChatResponse.builder()
                .chatroomId(chatroomId)
                .userId(userId)
                .userNickname(userNickname)
                .chatId(chatId)
                .content(content)
                .createdAt(createdAt)
                .build();
    }
}
