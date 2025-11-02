package org.example.ootoutfitoftoday.domain.userimage.service.command;

import org.example.ootoutfitoftoday.domain.userimage.entity.UserImage;

public interface UserImageCommandService {

    // UserImage 소프트 딜리트
    void softDeleteUserImage(UserImage userImage);
}
