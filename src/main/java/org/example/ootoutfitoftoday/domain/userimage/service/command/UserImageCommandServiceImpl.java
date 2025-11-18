package org.example.ootoutfitoftoday.domain.userimage.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.userimage.entity.UserImage;
import org.example.ootoutfitoftoday.domain.userimage.exception.UserImageErrorCode;
import org.example.ootoutfitoftoday.domain.userimage.exception.UserImageException;
import org.example.ootoutfitoftoday.domain.userimage.repository.UserImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserImageCommandServiceImpl implements UserImageCommandService {

    private final UserImageRepository userImageRepository;

    @Override
    public void softDeleteUserImage(UserImage userImage) {
        userImage.softDelete();
        userImageRepository.save(userImage);
    }

    @Override
    public UserImage createAndSave(Image image) {
        if (!userImageRepository.existsByImageId(image.getId())) {
            log.warn("이미지를 찾을 수 없음 - image: {}", image.getId());
            throw new UserImageException(UserImageErrorCode.USER_IMAGE_NOT_FOUND);
        }

        UserImage userImage = UserImage.create(image);

        return userImageRepository.save(userImage);
    }
}