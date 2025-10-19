package org.example.ootoutfitoftoday.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.user.entity.User;

@Getter
@Builder
@RequiredArgsConstructor
public class UserGetResponse {

    private final String loginId;
    private final String email;
    private final String username;
    private final String nickname;
    private final String phoneNumber;

    public static UserGetResponse from(User user) {

        return UserGetResponse.builder()
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}