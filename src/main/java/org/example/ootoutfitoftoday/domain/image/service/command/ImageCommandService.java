package org.example.ootoutfitoftoday.domain.image.service.command;

import org.example.ootoutfitoftoday.domain.image.dto.request.ImageSaveRequest;
import org.example.ootoutfitoftoday.domain.image.dto.request.PresignedUrlRequest;
import org.example.ootoutfitoftoday.domain.image.dto.response.ImageSaveResponse;
import org.example.ootoutfitoftoday.domain.image.dto.response.PresignedUrlResponse;

public interface ImageCommandService {

    PresignedUrlResponse generatePresignedUrl(Long userId, PresignedUrlRequest request);

    ImageSaveResponse saveImage(ImageSaveRequest request);
}