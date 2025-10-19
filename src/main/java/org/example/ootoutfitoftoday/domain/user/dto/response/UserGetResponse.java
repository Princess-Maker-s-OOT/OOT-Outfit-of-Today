package org.example.ootoutfitoftoday.domain.user.dto.response;

import lombok.Getter;
import org.example.ootoutfitoftoday.domain.user.entity.User;

@Getter
public class UserGetResponse {

    private final String loginId;
    private final String email;
    private final String username;
    private final String nickname;
    private final String phoneNumber;

    public UserGetResponse(
            String loginId,
            String email,
            String username,
            String nickname,
            String phoneNumber
    ) {
        this.loginId = loginId;
        this.email = email;
        this.username = username;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }

    public static UserGetResponse from(User user) {
        return new UserGetResponse(
                user.getLoginId(),
                user.getEmail(),
                user.getUsername(),
                user.getNickname(),
                user.getPhoneNumber()
        );
    }
}