package org.example.ootoutfitoftoday.domain.userimage.service.command;

import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.userimage.entity.UserImage;

public interface UserImageCommandService {

    void softDeleteUserImage(UserImage userImage);

    UserImage createAndSave(Image image);
}