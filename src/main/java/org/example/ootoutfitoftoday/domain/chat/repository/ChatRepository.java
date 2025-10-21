package org.example.ootoutfitoftoday.domain.chat.repository;

import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findFirstByChatroomAndIsDeletedFalseOrderByCreatedAtDesc(Chatroom chatroom);


    List<Chat> findByChatroomAndIsDeletedFalseOrderByCreatedAtDesc(Chatroom chatroom);
}
