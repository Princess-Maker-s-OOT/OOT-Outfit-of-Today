package org.example.ootoutfitoftoday.domain.chat.repository;

import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
