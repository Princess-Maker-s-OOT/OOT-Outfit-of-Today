package org.example.ootoutfitoftoday.domain.closetclotheslink.repository;

import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClosetClothesLinkRepository extends JpaRepository<ClosetClothesLink, Long> {

    boolean existsByClosetIdAndClothesId(Long closetId, Long clothesId);

    Page<ClosetClothesLink> findAllByClosetId(Long closetId, Pageable pageable);

    Optional<ClosetClothesLink> findByClosetIdAndClothesIdAndIsDeletedFalse(Long closetId, Long clothesId);
}
