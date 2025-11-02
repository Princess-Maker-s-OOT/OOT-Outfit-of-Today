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
@Table(name = "users")
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

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false, length = 50)
    private String tradeAddress;

    // TODO
    @Column(nullable = true, columnDefinition = "POINT SRID 4326", updatable = false, insertable = false)
    private String tradeLocation;

    // 현재 프로필 이미지 URL(소셜 로그인 또는 직접 업로드)
    @Column(nullable = true, length = 500)
    private String imageUrl;

    // 사용자가 직접 업로드한 이미지
    @OneToOne(fetch = FetchType.LAZY)    // cascade = CascadeType.ALL, orphanRemoval = true)    // 생명 주기를 함께 함
    @JoinColumn(name = "user_image_id")
    private UserImage userImage;

    // 로그인 타입 추가(LOGIN_ID, SOCIAL 구분)
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    // 소셜 로그인 제공자: GOOGLE, KAKAO, NAVER 등
    @Column(nullable = true, length = 10)
    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;

    // 소셜 ID(소셜 로그인 시 고유 식별자 - Google의 sub)
    @Column(nullable = true, unique = true, length = 100)
    private String socialId;

    // 옷장 연관관계
    @OneToMany(mappedBy = "user")
    private List<Closet> closets = new ArrayList<>();

    // 중간테이블
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

    // 기존의 일반 회원가입용
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

    // 소셜 회원가입용
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
                .password("")
                .phoneNumber("")
                .role(UserRole.ROLE_USER)
                .tradeAddress(DefaultLocationConstants.DEFAULT_TRADE_ADDRESS)
                .tradeLocation(null)
                .imageUrl(imageUrl)
                .loginType(LoginType.SOCIAL)
                .socialProvider(provider)
                .socialId(socialId)
                .build();
    }

    // 소셜 계정 연동용 메서드
    public void linkSocialAccount(
            SocialProvider socialProvider,
            String socialId,
            String imageUrl
    ) {
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        // 로그인 타입 소셜로 변경
        this.loginType = LoginType.SOCIAL;

        // 소셜 이미지 URL이 있고, 기존 이미지 URL이 null인 경우에만 업데이트
        if (imageUrl != null && this.imageUrl == null) {
            this.imageUrl = imageUrl;
        }
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

    // 회원정보 업데이트 관련 메서드
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

    // 프로필 이미지 업데이트
    public void updateProfileImage(Image image) {

        if (this.userImage == null) {
            // 없는 경우 새로 생성
            this.userImage = UserImage.create(image);
        } else {
            // 있는 경우 Image만 교체
            this.userImage.updateImage(image);
        }

        // imageUrl도 함께 업데이트
        this.imageUrl = image.getUrl();
    }

    // 프로필 이미지 삭제
    public void removeProfileImage() {

        this.userImage = null;
        this.imageUrl = null;
    }
}
