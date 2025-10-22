package org.example.ootoutfitoftoday.domain.image.repository;

import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
