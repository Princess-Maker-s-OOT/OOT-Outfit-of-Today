package org.example.ootoutfitoftoday.domain.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Presigned URL 생성 요청 DTO
public record PresignedUrlRequest(

        @NotBlank(message = "파일명은 필수입니다.")
        String fileName,

        @NotBlank(message = "이미지 타입은 필수입니다.")
        @Pattern(regexp = "^(closet|clothes|salepost|user)$",
                message = "이미지 타입은 closet, clothes, salepost, user 중 하나여야 합니다.")
        String type
) {
}