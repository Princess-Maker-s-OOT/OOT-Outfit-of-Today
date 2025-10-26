package org.example.ootoutfitoftoday.domain.image.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// 이미지 메타데이터 저장 요청 DTO
public record ImageSaveRequest(

        @NotBlank(message = "파일명은 필수입니다.")
        String fileName,

        @NotBlank(message = "URL은 필수입니다.")
        String url,

        @NotBlank(message = "S3 Key는 필수입니다.")
        String s3Key,

        @NotBlank(message = "Content Type은 필수입니다.")
        String contentType,

        @NotBlank(message = "이미지 타입은 필수입니다.")
        @Pattern(regexp = "^(CLOSET|CLOTHES|SALEPOST|USER)$",
                message = "이미지 타입은 CLOSET, CLOTHES, SALEPOST, USER 중 하나여야 합니다.")
        String type,

        @NotNull(message = "파일 크기는 필수입니다.")
        @Min(value = 1, message = "파일 크기는 1바이트 이상이어야 합니다.")
        Long size
) {
}