package org.example.ootoutfitoftoday.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.common.util.DefaultLocationConstants;
import org.example.ootoutfitoftoday.domain.auth.enums.LoginType;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUserId;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;
import org.example.ootoutfitoftoday.domain.userimage.entity.UserImage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users", indexes = {
        @Index(name = "idx_login_id", columnList = "loginId"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_nickname", columnList = "nickname"),
        @Index(name = "idx_phone_number", columnList = "phoneNumber"),
        @Index(name = "idx_social_provider_id", columnList = "socialProvider, socialId"),
        @Index(name = "idx_is_deleted", columnList = "isDeleted")
})
public class User extends BaseEntity {

    private static final String SOCIAL_LOGIN_ID_PREFIX = "SOCIAL_";

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

    @Column(nullable = true, length = 255)
    private String password;

    @Column(nullable = true, unique = true, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false, length = 50)
    private String tradeAddress;

    @Column(nullable = true, columnDefinition = "POINT SRID 4326", updatable = false, insertable = false)
    private String tradeLocation;

    @Column(nullable = true, length = 500)
    private String imageUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_image_id")
    private UserImage userImage;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column(nullable = true, length = 10)
    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;

    @Column(nullable = true, unique = true, length = 100)
    private String socialId;

    @OneToMany(mappedBy = "user")
    private List<Closet> closets = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ChatParticipatingUser> chatParticipatingUsers = new ArrayList<>();

    @Builder//(access = AccessLevel.PROTECTED)
    private User(
            String loginId,
            String email,
            String nickname,
            String username,
            String password,
            String phoneNumber,
            UserRole role,
            String tradeAddress,
            String tradeLocation,
            String imageUrl,
            LoginType loginType,
            SocialProvider socialProvider,
            String socialId
    ) {
        this.loginId = loginId;
        this.email = email;
        this.nickname = nickname;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.tradeAddress = tradeAddress;
        this.tradeLocation = tradeLocation;
        this.imageUrl = imageUrl;
        this.loginType = loginType;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
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
                .tradeAddress(DefaultLocationConstants.DEFAULT_TRADE_ADDRESS)
                .tradeLocation(DefaultLocationConstants.DEFAULT_TRADE_LOCATION)
                .imageUrl(imageUrl)
                .loginType(LoginType.LOGIN_ID)
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
                .role(UserRole.ROLE_ADMIN)
                .tradeAddress(DefaultLocationConstants.DEFAULT_TRADE_ADDRESS)
                .tradeLocation(DefaultLocationConstants.DEFAULT_TRADE_LOCATION)
                .imageUrl(null)
                .loginType(LoginType.LOGIN_ID)
                .build();
    }

    public static User createFromSocial(
            String email,
            String nickname,
            String username,
            String imageUrl,
            SocialProvider provider,
            String socialId
    ) {
        return User.builder()
                .loginId(SOCIAL_LOGIN_ID_PREFIX + UUID.randomUUID().toString().substring(0, 18))
                .email(email)
                .nickname(nickname)
                .username(username)
                .password(null)
                .phoneNumber(null)
                .role(UserRole.ROLE_USER)
                .tradeAddress(DefaultLocationConstants.DEFAULT_TRADE_ADDRESS)
                .tradeLocation(null)
                .imageUrl(imageUrl)
                .loginType(LoginType.SOCIAL)
                .socialProvider(provider)
                .socialId(socialId)
                .build();
    }

    public void linkSocialAccount(
            SocialProvider socialProvider,
            String socialId,
            String imageUrl
    ) {
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.loginType = LoginType.SOCIAL;

        if (imageUrl != null && this.imageUrl == null) {
            this.imageUrl = imageUrl;
        }
    }

    public void addChatParticipatingUser(Chatroom chatroom) {
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

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updateTradeLocation(String tradeAddress, String tradeLocation) {
        this.tradeAddress = tradeAddress;
        this.tradeLocation = tradeLocation;
    }

    public void changeProfileImage(Image newImage) {
        this.userImage.updateImage(newImage);
        this.imageUrl = newImage.getUrl();
    }

    public void assignProfileImage(UserImage userImage) {
        this.userImage = userImage;
        this.imageUrl = userImage.getImage().getUrl();
    }

    public void removeProfileImage() {
        this.userImage = null;
        this.imageUrl = null;
    }
}