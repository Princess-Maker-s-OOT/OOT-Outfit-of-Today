package org.example.ootoutfitoftoday.domain.chatparticipatinguser.repository;

import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUserId;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatParticipatingUserRepository extends JpaRepository<ChatParticipatingUser, ChatParticipatingUserId> {
    
    List<ChatParticipatingUser> findByUserAndDeletedIsFalse(User user);
}
