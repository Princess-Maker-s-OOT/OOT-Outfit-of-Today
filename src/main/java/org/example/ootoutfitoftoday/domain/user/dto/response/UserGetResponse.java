package org.example.ootoutfitoftoday.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.enums.UserRole;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Getter
@Builder
@RequiredArgsConstructor
public class UserGetResponse {

    private final String imageUrl;
    private final String loginId;
    private final String email;
    private final String username;
    private final String nickname;
    private final String phoneNumber;
    private final UserRole role;

    public static UserGetResponse from(User user) {

        return UserGetResponse.builder()
                .imageUrl(user.getImageUrl())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }
}