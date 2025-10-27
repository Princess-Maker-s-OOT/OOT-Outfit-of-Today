package org.example.ootoutfitoftoday.domain.image.service.command;

import org.example.ootoutfitoftoday.domain.image.dto.request.ImageSaveRequest;
import org.example.ootoutfitoftoday.domain.image.dto.request.PresignedUrlRequest;
import org.example.ootoutfitoftoday.domain.image.dto.response.ImageSaveResponse;
import org.example.ootoutfitoftoday.domain.image.dto.response.PresignedUrlResponse;

public interface ImageCommandService {

    // Presigned URL 생성
    PresignedUrlResponse generatePresignedUrl(
            Long userId,
            PresignedUrlRequest request
    );

    // 이미지 메타데이터 저장
    ImageSaveResponse saveImage(ImageSaveRequest request);
}