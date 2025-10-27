package org.example.ootoutfitoftoday.domain.image.service.query;

import org.example.ootoutfitoftoday.domain.image.entity.Image;

public interface ImageQueryService {

    Image findImageById(Long imageId);
}
