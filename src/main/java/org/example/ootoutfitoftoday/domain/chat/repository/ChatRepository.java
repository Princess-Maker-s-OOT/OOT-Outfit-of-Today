package org.example.ootoutfitoftoday.domain.chat.repository;

import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long>, CustomChatRepository {

    Optional<Chat> findFirstByChatroomAndIsDeletedFalseOrderByCreatedAtDesc(Chatroom chatroom);

    Slice<Chat> findByChatroomAndIsDeletedFalseOrderByCreatedAtDesc(Chatroom chatroom, Pageable pageable);

    /**
     * 채팅방에 삭제되지 않은 채팅이 존재하는지 확인
     * (거래 시작 전 채팅 내역 존재 여부 확인용)
     */
    boolean existsByChatroomIdAndIsDeletedFalse(Long chatroomId);
}
