package org.example.ootoutfitoftoday.domain.chatroom.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
public class ChatroomResponse {

    private final String otherUserNickname;
    private final String finalChat;
    private final Duration afterFinalChatTime;

    @Builder(access = AccessLevel.PROTECTED)
    private ChatroomResponse(
            String otherUserNickname,
            String finalChat,
            Duration afterFinalChatTime
    ) {
        this.otherUserNickname = otherUserNickname;
        this.finalChat = finalChat;
        this.afterFinalChatTime = afterFinalChatTime;
    }

    public static ChatroomResponse of(
            String otherUserNickname,
            String finalChat,
            Duration afterFinalChatTime
    ) {

        return ChatroomResponse.builder()
                .otherUserNickname(otherUserNickname)
                .finalChat(finalChat)
                .afterFinalChatTime(afterFinalChatTime)
                .build();
    }
}