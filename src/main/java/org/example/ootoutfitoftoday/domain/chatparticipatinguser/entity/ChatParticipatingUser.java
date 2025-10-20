package org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chat_participating_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipatingUser {

    @EmbeddedId
    private ChatParticipatingUserId id;

    @MapsId("chatroomId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private Chatroom chatroom;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JoinColumn(name = "deleted_at")
    private LocalDateTime deletedAt;

    @JoinColumn(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Builder(access = AccessLevel.PROTECTED)
    private ChatParticipatingUser(
            ChatParticipatingUserId id,
            Chatroom chatroom,
            User user
    ) {
        this.id = id;
        this.chatroom = chatroom;
        this.user = user;
        deletedAt = null;
        isDeleted = false;
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

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }
}
