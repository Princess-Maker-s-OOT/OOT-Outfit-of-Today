package org.example.ootoutfitoftoday.domain.clothes.repository;

import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClothesRepository extends JpaRepository<Clothes, Long>, CustomClothesRepository {

    Optional<Clothes> findByIdAndUserId(Long id, Long userId);

    Optional<Clothes> findByIdAndIsDeletedFalse(Long id);
}
