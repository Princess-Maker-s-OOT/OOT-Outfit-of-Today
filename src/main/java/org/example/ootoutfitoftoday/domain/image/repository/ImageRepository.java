package org.example.ootoutfitoftoday.domain.image.repository;

import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByIdAndIsDeletedFalse(Long id);

    Optional<Image> findByS3KeyAndIsDeletedFalse(String s3Key);

    @Query("""
            SELECT i
            FROM Image i
            WHERE i.id IN :imageIds
              AND i.isDeleted = false
            """)
    List<Image> findAllByIdInAndIsDeletedFalse(List<Long> imageIds);
}