package org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query;

import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.util.List;

public interface ChatParticipatingUserQueryService {

    // 유저를 통해서 전체 채팅방에 대한 객체를 얻기 위한 메서드
    List<ChatParticipatingUser> getChatParticipatingUsers(User user);
}
