package org.example.ootoutfitoftoday.domain.closet.repository;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosetRepository extends JpaRepository<Closet, Long> {

    /**
     * 공개 옷장 리스트 조회
     * 조건: isPublic = true (Soft Delete 조건은 @Where에 의해 자동 적용됨)
     */
    Page<Closet> findAllByIsPublicTrue(Pageable pageable);


    /**
     * 내 옷장 리스트 조회
     * 조건: userId = ? (Soft Delete 조건은 @Where에 의해 자동 적용됨)
     */
    Page<Closet> findAllByUser_Id( // 기존 findAllByUser_IdAndIsDeletedFalse 에서 AndIsDeletedFalse 제거
                                   Long userId,
                                   Pageable pageable
    );
}