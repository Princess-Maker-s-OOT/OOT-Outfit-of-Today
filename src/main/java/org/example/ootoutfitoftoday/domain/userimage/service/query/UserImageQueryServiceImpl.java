package org.example.ootoutfitoftoday.domain.userimage.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.userimage.entity.UserImage;
import org.example.ootoutfitoftoday.domain.userimage.exception.UserImageErrorCode;
import org.example.ootoutfitoftoday.domain.userimage.exception.UserImageException;
import org.example.ootoutfitoftoday.domain.userimage.repository.UserImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserImageQueryServiceImpl implements UserImageQueryService {

    private final UserImageRepository userImageRepository;

    @Override
    public UserImage findByIdAndIsDeletedFalse(Long userImageId) {

        return userImageRepository.findByIdAndIsDeletedFalse(userImageId)
                .orElseThrow(() -> new UserImageException(UserImageErrorCode.USER_IMAGE_NOT_FOUND));
    }
}
