package org.example.ootoutfitoftoday.domain.userimage.repository;

import org.example.ootoutfitoftoday.domain.userimage.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {

    Optional<UserImage> findByIdAndIsDeletedFalse(Long id);

    boolean existsByImageId(Long id);
}