package org.example.ootoutfitoftoday.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserUpdateProfileImageRequest {

    @NotNull(message = "이미지 ID는 필수입니다.")
    private Long imageId;
}
