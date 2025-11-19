package org.example.ootoutfitoftoday.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.auth.enums.LoginType;
import org.example.ootoutfitoftoday.domain.auth.enums.SocialProvider;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public class UserCacheDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String loginId;
    private final String email;
    private final String nickname;
    private final String username;
    private final String password;
    private final String phoneNumber;
    private final UserRole role;
    private final String tradeAddress;
    private final String imageUrl;
    private final LoginType loginType;
    private final SocialProvider socialProvider;
    private final String socialId;
    private final boolean isDeleted;
    private final LocalDateTime deletedAt;

    @JsonCreator
    public UserCacheDto(
            @JsonProperty("id") Long id,
            @JsonProperty("loginId") String loginId,
            @JsonProperty("email") String email,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("role") UserRole role,
            @JsonProperty("tradeAddress") String tradeAddress,
            @JsonProperty("imageUrl") String imageUrl,
            @JsonProperty("loginType") LoginType loginType,
            @JsonProperty("socialProvider") SocialProvider socialProvider,
            @JsonProperty("socialId") String socialId,
            @JsonProperty("deleted") boolean isDeleted,
            @JsonProperty("deletedAt") LocalDateTime deletedAt
    ) {
        this.id = id;
        this.loginId = loginId;
        this.email = email;
        this.nickname = nickname;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.tradeAddress = tradeAddress;
        this.imageUrl = imageUrl;
        this.loginType = loginType;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
    }

    public static UserCacheDto from(User user) {

        return new UserCacheDto(
                user.getId(),
                user.getLoginId(),
                user.getEmail(),
                user.getNickname(),
                user.getUsername(),
                user.getPassword(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getTradeAddress(),
                user.getImageUrl(),
                user.getLoginType(),
                user.getSocialProvider(),
                user.getSocialId(),
                user.isDeleted(),
                user.getDeletedAt()
        );
    }

    public boolean isDeleted() {

        return isDeleted || deletedAt != null;
    }
}