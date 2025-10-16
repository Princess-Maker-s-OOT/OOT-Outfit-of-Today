package org.example.ootoutfitoftoday.domain.chatparticipatinguser.repository;

import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUserId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipatingUserRepository extends JpaRepository<ChatParticipatingUser, ChatParticipatingUserId> {
}
