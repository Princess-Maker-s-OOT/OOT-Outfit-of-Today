package org.example.ootoutfitoftoday.domain.image.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.exception.ImageErrorCode;
import org.example.ootoutfitoftoday.domain.image.exception.ImageException;
import org.example.ootoutfitoftoday.domain.image.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageQueryServiceImpl implements ImageQueryService {

    private final ImageRepository imageRepository;

    // 지정된 ID에 해당하는 이미지를 조회
    @Override
    public Image findImageById(Long imageId) {

        return imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageException(ImageErrorCode.IMAGE_NOT_FOUND));
    }
}