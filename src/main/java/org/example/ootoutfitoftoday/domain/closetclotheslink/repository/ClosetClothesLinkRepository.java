package org.example.ootoutfitoftoday.domain.closetclotheslink.repository;

import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosetClothesLinkRepository extends JpaRepository<ClosetClothesLink, Long> {

    // 해당 옷장에 해당 옷이 이미 등록되어 있는지 확인
    boolean existsByClosetIdAndClothesId(Long closetId, Long clothesId);
}
