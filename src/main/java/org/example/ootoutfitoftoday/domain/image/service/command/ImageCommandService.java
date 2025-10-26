package org.example.ootoutfitoftoday.domain.image.service.command;

import org.example.ootoutfitoftoday.domain.image.dto.request.PresignedUrlRequest;
import org.example.ootoutfitoftoday.domain.image.dto.response.PresignedUrlResponse;

public interface ImageCommandService {

    // Presigned URL 생성
    PresignedUrlResponse generatePresignedUrl(
            Long userId,
            PresignedUrlRequest request
    );
}