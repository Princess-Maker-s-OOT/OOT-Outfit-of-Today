package org.example.ootoutfitoftoday.domain.closet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;

/**
 * 옷장 정보 수정을 위한 요청 DTO
 * - 이미지는 1장만 수정 가능하며, 선택 사항(nullable)
 */
@Builder(access = AccessLevel.PRIVATE)
public record ClosetUpdateRequest(

        @NotBlank(message = "옷장 이름은 필수입니다.")
        @Size(max = 100, message = "옷장 이름은 100자를 초과할 수 없습니다.")
        String name,

        @Size(max = 255, message = "옷장 설명은 255자를 초과할 수 없습니다.")
        String description,

        Long imageId,

        @NotNull(message = "공개 여부는 필수입니다.")
        Boolean isPublic
) {
    public static ClosetUpdateRequest of(
            String name,
            String description,
            Long imageId,
            Boolean isPublic
    ) {

        return ClosetUpdateRequest.builder()
                .name(name)
                .description(description)
                .imageId(imageId)
                .isPublic(isPublic)
                .build();
    }
}