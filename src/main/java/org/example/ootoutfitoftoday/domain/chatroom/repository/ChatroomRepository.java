package org.example.ootoutfitoftoday.domain.chatroom.repository;

import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {

    /**
     * 사용자와 판매글로 채팅방 조회
     * (거래 시작 시 채팅방 존재 여부 확인용)
     */
    @Query("""
        SELECT c FROM Chatroom c
        WHERE c.salePost.id = :salePostId
          AND c.isDeleted = false
          AND EXISTS (
            SELECT 1 FROM ChatParticipatingUser cpu
            WHERE cpu.chatroom = c
              AND cpu.user.id = :userId
              AND cpu.isDeleted = false
          )
        """)
    Optional<Chatroom> findByUserAndSalePost(
            @Param("userId") Long userId,
            @Param("salePostId") Long salePostId
    );
}
