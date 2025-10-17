package org.example.ootoutfitoftoday.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUserId;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 25)
    private String loginId;

    @Column(nullable = false, unique = true, length = 60)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false, length = 60)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = true, length = 500)
    private String imageUrl;

    // 옷장 연관관계
//    @OneToMany(mappedBy = "user")
//    private List<Closet> closets = new ArrayList<>();

    // 중간테이블
    @OneToMany(mappedBy = "user")
    private List<ChatParticipatingUser> chatParticipatingUsers = new ArrayList<>();

    @Builder(access = AccessLevel.PROTECTED)
    private User(
            String loginId,
            String email,
            String nickname,
            String username,
            String password,
            String phoneNumber,
            UserRole role,
            String imageUrl
    ) {
        this.loginId = loginId;
        this.email = email;
        this.nickname = nickname;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.imageUrl = imageUrl;
    }

    public static User create(
            String loginId,
            String email,
            String nickname,
            String username,
            String password,
            String phoneNumber,
            UserRole role,
            String imageUrl
    ) {

        return User.builder()
                .loginId(loginId)
                .email(email)
                .nickname(nickname)
                .username(username)
                .password(password)
                .phoneNumber(phoneNumber)
                .role(role)
                .imageUrl(imageUrl)
                .build();
    }

    public static User createAdmin(
            String loginId,
            String email,
            String nickname,
            String username,
            String password,
            String phoneNumber
    ) {
        return User.builder()
                .loginId(loginId)
                .email(email)
                .nickname(nickname)
                .username(username)
                .password(password)
                .phoneNumber(phoneNumber)
                .role(UserRole.ROLE_ADMIN)    // 고정값: 항상 ADMIN
                .imageUrl(null)               // 고정값: 관리자 이미지 파일 제외
                .build();
    }

    // 헬퍼 메서드
    public void addChatParticipatingUser(Chatroom chatroom) {
        // 사용자가 이미 채팅방에 참여하고 있는지 확인하여 중복 추가를 방지합니다.
        boolean alreadyExists = this.chatParticipatingUsers.stream()
                .anyMatch(p -> p.getChatroom().getId().equals(chatroom.getId()));
        if (alreadyExists) {
            return;
        }

        ChatParticipatingUserId chatParticipatingUserId = ChatParticipatingUserId.create(
                chatroom.getId(),
                this.id
        );
        ChatParticipatingUser chatParticipatingUser = ChatParticipatingUser.create(
                chatParticipatingUserId,
                chatroom,
                this
        );
        this.chatParticipatingUsers.add(chatParticipatingUser);
        chatroom.getChatParticipatingUsers().add(chatParticipatingUser);
    }
}
