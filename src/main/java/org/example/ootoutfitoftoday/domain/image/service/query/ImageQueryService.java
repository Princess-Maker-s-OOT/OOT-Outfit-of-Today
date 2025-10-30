package org.example.ootoutfitoftoday.domain.image.service.query;

import org.example.ootoutfitoftoday.domain.image.entity.Image;

public interface ImageQueryService {

    Image findImageById(Long imageId);

    // 소프트 딜리트된 이미지 필터링 조회
    Image findByIdAndIsDeletedFalse(Long imageId);
}
