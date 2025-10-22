package org.example.ootoutfitoftoday.domain.closetclotheslink.repository;

import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClosetClothesLinkRepository extends JpaRepository<ClosetClothesLink, Long> {

    // 해당 옷장에 해당 옷이 이미 등록되어 있는지 확인
    boolean existsByClosetIdAndClothesId(Long closetId, Long clothesId);

    // 특정 옷장에 등록된 옷 리스트 조회
    Page<ClosetClothesLink> findAllByClosetId(Long closetId, Pageable pageable);

    /**
     * 특정 옷장과 특정 옷의 연결 조회
     * 옷장: ClosetId
     * 옷: ClothesId
     * 삭제: Isdeleted = false
     */
    Optional<ClosetClothesLink> findByClosetIdAndClothesIdAndIsDeletedFalse(
            Long closetId,
            Long clothesId
    );
}
