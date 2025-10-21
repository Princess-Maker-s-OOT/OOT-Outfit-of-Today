package org.example.ootoutfitoftoday.domain.chatroom.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUserId;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "chatrooms")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Chatroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * OneToMany를 사용하는 이유: 디비에 접근하지 않고 해당 데이터를 이용하기 위함
     * 주의할 점: 데이터베이스와의 일관성을 위해 리스트 내 데이터의 수정 x
     */
    @OneToMany(mappedBy = "chatroom")
    private List<ChatParticipatingUser> chatParticipatingUsers = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 헬퍼 메서드
    public void addChatParticipatingUser(User user) {
        // 사용자가 이미 채팅방에 참여하고 있는지 확인하여 중복 추가를 방지합니다.
        boolean alreadyExists = this.chatParticipatingUsers.stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));
        if (alreadyExists) {
            return;
        }

        ChatParticipatingUserId chatParticipatingUserId = ChatParticipatingUserId.create(
                this.id,
                user.getId()
        );
        ChatParticipatingUser chatParticipatingUser = ChatParticipatingUser.create(
                chatParticipatingUserId,
                this, user
        );
        this.chatParticipatingUsers.add(chatParticipatingUser);
        user.getChatParticipatingUsers().add(chatParticipatingUser);
    }
}

