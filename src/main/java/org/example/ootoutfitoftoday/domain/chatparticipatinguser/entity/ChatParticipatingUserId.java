package org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipatingUserId implements Serializable {

    @Column(name = "chatroom_id", nullable = false)
    private Long chatroomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder(access = AccessLevel.PROTECTED)
    private ChatParticipatingUserId(
            Long chatroomId,
            Long userId
    ) {
        this.chatroomId = chatroomId;
        this.userId = userId;
    }

    public static ChatParticipatingUserId create(
            Long chatroomId,
            Long userId
    ) {

        return new ChatParticipatingUserId(
                chatroomId,
                userId
        );
    }
}
