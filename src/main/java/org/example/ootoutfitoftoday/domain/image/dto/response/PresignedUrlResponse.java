package org.example.ootoutfitoftoday.domain.image.dto.response;

public record PresignedUrlResponse(
        String presignedUrl,
        String fileUrl,
        String s3Key,
        int expiresIn
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