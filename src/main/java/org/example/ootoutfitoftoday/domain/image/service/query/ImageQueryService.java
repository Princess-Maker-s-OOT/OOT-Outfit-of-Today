package org.example.ootoutfitoftoday.domain.image.service.query;

import org.example.ootoutfitoftoday.domain.image.entity.Image;

import java.util.List;

public interface ImageQueryService {

    Image findImageById(Long imageId);

    List<Image> findAllByIdIn(List<Long> imageIds);
}
