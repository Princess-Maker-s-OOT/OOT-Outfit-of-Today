package org.example.ootoutfitoftoday.domain.chatroom.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
public class ChatroomResponse {

    //    - 상대방 이름 : user.getNickname();
    private String otherUsername;
    //    - 마지막 채팅 : chat.getContent()
    private String finalChat;
    //    - 현재 시간 - 마지막 채팅 시간 : LocalDateTime.now() - chat.getCreatedAt() : 정렬의 기준
    private Duration afterFinalChatTime;
    //    - 읽지 않은 채팅 개수
    private int noReadChats;

    @Builder(access = AccessLevel.PROTECTED)
    private ChatroomResponse(
            String otherUsername,
            String finalChat,
            Duration afterFinalChatTime,
            int noReadChats
    ) {
        this.otherUsername = otherUsername;
        this.finalChat = finalChat;
        this.afterFinalChatTime = afterFinalChatTime;
        this.noReadChats = noReadChats;
    }

    public static ChatroomResponse from(
            String otherUsername,
            String finalChat,
            Duration afterFinalChatTime,
            int noReadChats
    ) {

        return ChatroomResponse.builder()
                .otherUsername(otherUsername)
                .finalChat(finalChat)
                .afterFinalChatTime(afterFinalChatTime)
                .noReadChats(noReadChats)
                .build();
    }
}
