package org.example.ootoutfitoftoday.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class UserUpdateResponse {

    private final String email;
    private final String nickname;
    private final String username;
    private final String phoneNumber;

    public static UserUpdateResponse of(
            String email,
            String nickname,
            String username,
            String phoneNumber
    ) {
        return UserUpdateResponse.builder()
                .email(email)
                .nickname(nickname)
                .username(username)
                .phoneNumber(phoneNumber)
                .build();
    }
}
