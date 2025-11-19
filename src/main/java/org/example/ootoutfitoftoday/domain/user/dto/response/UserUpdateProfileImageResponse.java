package org.example.ootoutfitoftoday.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class UserUpdateProfileImageResponse {

    private final Long userId;
    private final String imageUrl;

    public static UserUpdateProfileImageResponse of(Long userId, String profileImageUrl) {

        return UserUpdateProfileImageResponse.builder()
                .userId(userId)
                .imageUrl(profileImageUrl)
                .build();
    }
}
