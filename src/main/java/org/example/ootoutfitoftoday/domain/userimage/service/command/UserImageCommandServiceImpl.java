package org.example.ootoutfitoftoday.domain.userimage.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.userimage.entity.UserImage;
import org.example.ootoutfitoftoday.domain.userimage.repository.UserImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserImageCommandServiceImpl implements UserImageCommandService {

    private final UserImageRepository userImageRepository;

    @Override
    public void softDeleteUserImage(UserImage userImage) {

        userImage.softDelete(); // BaseEntity의 softDelete() 메서드 호출
        userImageRepository.save(userImage);
    }

    @Override
    public UserImage createAndSave(Image image) {

        UserImage userImage = UserImage.create(image);

        return userImageRepository.save(userImage);
    }
}
