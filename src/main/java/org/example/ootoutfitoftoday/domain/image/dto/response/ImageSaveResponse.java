package org.example.ootoutfitoftoday.domain.image.dto.response;

import org.example.ootoutfitoftoday.domain.image.entity.Image;

import java.time.LocalDateTime;

// 이미지 메타데이터 저장 응답 DTO
public record ImageSaveResponse(
        Long id,
        String fileName,
        String url,
        String s3Key,
        String contentType,
        String type,
        Long size,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ImageSaveResponse from(Image image) {

        return new ImageSaveResponse(
                image.getId(),
                image.getFileName(),
                image.getUrl(),
                image.getS3Key(),
                image.getContentType(),
                image.getType().name(),
                image.getSize(),
                image.getCreatedAt(),
                image.getUpdatedAt()
        );
    }
}