package org.example.ootoutfitoftoday.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.user.entity.User;

@Entity
@Getter
@Table(name = "chats")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Chatroom chatroom;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder(access = AccessLevel.PROTECTED)
    private Chat(
            String content,
            Chatroom chatroom,
            User user
    ) {
        this.content = content;
        this.chatroom = chatroom;
        this.user = user;
    }

    public static Chat create(
            String content,
            Chatroom chatroom,
            User user
    ) {

        return Chat.builder()
                .content(content)
                .chatroom(chatroom)
                .user(user)
                .build();
    }
}
