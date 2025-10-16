package org.example.ootoutfitoftoday.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;

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

    @Builder(access = AccessLevel.PROTECTED)
    private Chat(String content) {
        this.content = content;
    }

    public static Chat create(String content) {

        return Chat.builder()
                .content(content)
                .build();
    }
}
