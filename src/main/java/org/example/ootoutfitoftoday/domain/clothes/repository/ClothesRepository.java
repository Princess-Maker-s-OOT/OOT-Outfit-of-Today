package org.example.ootoutfitoftoday.domain.clothes.repository;

import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesRepository extends JpaRepository<Clothes, Long> {
}
