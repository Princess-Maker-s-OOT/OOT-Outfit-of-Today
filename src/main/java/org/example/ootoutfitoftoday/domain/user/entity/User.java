package org.example.ootoutfitoftoday.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String loginId;

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Column(unique = true, nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(length = 500)
    private String imageUrl;

    // 중간테이블
//    @OneToMany(mappedBy = "chatRoom")
//    private List<ChatParticipatingUser> participants = new ArrayList<>();

    @Builder(access = AccessLevel.PROTECTED)
    public User(
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
}
