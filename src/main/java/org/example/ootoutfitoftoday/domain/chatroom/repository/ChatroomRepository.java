package org.example.ootoutfitoftoday.domain.chatroom.repository;

import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
}
