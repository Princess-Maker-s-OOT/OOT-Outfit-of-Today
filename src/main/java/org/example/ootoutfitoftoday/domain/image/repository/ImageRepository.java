package org.example.ootoutfitoftoday.domain.image.repository;

import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    // 삭제되지 않은 이미지 조회
    Optional<Image> findByIdAndIsDeletedFalse(Long id);

    // S3 Key로 이미지 조회
    Optional<Image> findByS3KeyAndIsDeletedFalse(String s3Key);

    // 여러 이미지 ID를 한 번에 조회
    @Query("""
            SELECT i
            FROM Image i
            WHERE i.id IN :imageIds
            """)
    List<Image> findAllByIdIn(List<Long> imageIds);
}