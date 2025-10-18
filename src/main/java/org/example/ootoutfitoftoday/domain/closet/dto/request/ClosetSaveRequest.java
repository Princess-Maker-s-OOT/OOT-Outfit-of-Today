package org.example.ootoutfitoftoday.domain.closet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClosetSaveRequest(

        // 임시: userId를 직접 받음 (나중에 인증으로 변 예정)
        @NotNull
        Long userId,

        @NotBlank(message = "옷장 이름은 필수입니다.")
        @Size(max = 100, message = "옷장 이름은 100자를 초과할 수 없습니다.")
        String name,

        @Size(max = 255, message = "옷장 설명은 255자를 초과할 수 없습니다.")
        String description,

        String imageUrl,

        @NotNull(message = "공개 여부는 필수입니다.")
        Boolean isPublic
) {
}

