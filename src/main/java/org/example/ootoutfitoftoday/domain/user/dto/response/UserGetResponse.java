package org.example.ootoutfitoftoday.domain.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.user.entity.User;

@Getter
@RequiredArgsConstructor
public class UserGetResponse {

    private final String loginId;
    private final String email;
    private final String username;
    private final String nickname;
    private final String phoneNumber;

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