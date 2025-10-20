package org.example.ootoutfitoftoday.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;

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

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    private Chatroom chatroom;

    @Builder(access = AccessLevel.PROTECTED)
    private Chat(
            String content,
            Chatroom chatroom
    ) {
        this.content = content;
        this.chatroom = chatroom;
        isRead = false;
    }

    public static Chat create(
            String content,
            Chatroom chatroom
    ) {

        return Chat.builder()
                .content(content)
                .chatroom(chatroom)
                .build();
    }
}
