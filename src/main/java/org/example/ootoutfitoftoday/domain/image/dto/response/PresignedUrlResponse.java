package org.example.ootoutfitoftoday.domain.image.dto.response;

/**
 * Presigned URL 생성 응답 DTO
 */
public record PresignedUrlResponse(

        String presignedUrl,  // Presigned URL (업로드용)
        String fileUrl,       // 파일 최종 URL (조회용)
        String s3Key,         // S3 객체 키
        int expiresIn         // 만료 시간 (초)
) {
    public static PresignedUrlResponse of(
            String presignedUrl,
            String fileUrl,
            String s3Key,
            int expiresIn
    ) {

        return new PresignedUrlResponse(
                presignedUrl,
                fileUrl,
                s3Key,
                expiresIn
        );
    }
}
