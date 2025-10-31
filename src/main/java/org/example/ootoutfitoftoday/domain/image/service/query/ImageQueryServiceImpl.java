package org.example.ootoutfitoftoday.domain.image.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.exception.ImageErrorCode;
import org.example.ootoutfitoftoday.domain.image.exception.ImageException;
import org.example.ootoutfitoftoday.domain.image.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    @Override
    public List<Image> findAllByIdInAndIsDeletedFalse(List<Long> imageIds) {

        // 중복 제거 + 입력 순서 유지
        Set<Long> uniqueIds = new LinkedHashSet<>(imageIds);

        List<Image> images = imageRepository.findAllByIdInAndIsDeletedFalse(new ArrayList<>(uniqueIds));

        if (images.size() != uniqueIds.size()) {

            throw new ImageException(ImageErrorCode.IMAGE_NOT_FOUND);
        }

        return images;
    }
}