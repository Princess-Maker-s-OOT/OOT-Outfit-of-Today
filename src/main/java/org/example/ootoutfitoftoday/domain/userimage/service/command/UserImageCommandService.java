package org.example.ootoutfitoftoday.domain.userimage.service.command;

import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.userimage.entity.UserImage;

public interface UserImageCommandService {

    // UserImage 소프트 딜리트
    void softDeleteUserImage(UserImage userImage);

    // Image를 받아서 UserImage 생성 및 저장
    UserImage createAndSave(Image image);
}
