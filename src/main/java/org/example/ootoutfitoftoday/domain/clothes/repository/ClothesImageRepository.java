package org.example.ootoutfitoftoday.domain.clothes.repository;

import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesImageRepository extends JpaRepository<ClothesImage, Long> {
}
