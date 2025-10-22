package org.example.ootoutfitoftoday.domain.chatroom.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
public class ChatroomResponse {

    //    - 상대방 이름 : user.getNickname();
    private final String otherUserNickname;
    //    - 마지막 채팅 : chat.getContent()
    private final String finalChat;
    //    - 현재 시간 - 마지막 채팅 시간 : LocalDateTime.now() - chat.getCreatedAt() : 정렬의 기준
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
