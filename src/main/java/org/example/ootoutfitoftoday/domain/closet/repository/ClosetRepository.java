package org.example.ootoutfitoftoday.domain.closet.repository;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosetRepository extends JpaRepository<Closet, Long> {

    /**
     * 공개 옷장 리스트 조회
     * 리스트(전체): all
     * 공개: IsPublic = true
     * 삭제: Isdeleted = false
     */
    Page<Closet> findAllByIsPublicTrueAndIsDeletedFalse(Pageable pageable);


    /**
     * 내 옷장 리스트 조회
     * 리스트(전체): all
     * 로그인한 사용자의 ID: userId
     * 삭제: Isdeleted = false
     */
    Page<Closet> findAllByUserIdAndIsDeletedFalse(
            Long userId,
            Pageable pageable
    );
}
