package org.example.ootoutfitoftoday.domain.userimage.service.query;

import org.example.ootoutfitoftoday.domain.userimage.entity.UserImage;

public interface UserImageQueryService {

    UserImage findByIdAndIsDeletedFalse(Long userImageId);
}
