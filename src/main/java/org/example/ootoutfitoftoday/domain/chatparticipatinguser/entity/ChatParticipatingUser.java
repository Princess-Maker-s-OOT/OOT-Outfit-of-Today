package org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.user.entity.User;

@Getter
@Entity
@Table(name = "chat_participating_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipatingUser {

    @EmbeddedId
    private ChatParticipatingUserId id;

    @MapsId("chatroomId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PROTECTED)
    private ChatParticipatingUser(
            ChatParticipatingUserId id,
            Chatroom chatroom,
            User user
    ) {
        this.id = id;
        this.chatroom = chatroom;
        this.user = user;
    }

    public static ChatParticipatingUser create(
            ChatParticipatingUserId id,
            Chatroom chatroom,
            User user
    ) {

        return new ChatParticipatingUser(
                id,
                chatroom,
                user
        );
    }
}
