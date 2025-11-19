package org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query;

import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.util.List;

public interface ChatParticipatingUserQueryService {

    List<ChatParticipatingUser> getChatParticipatingUsers(User user);

    List<ChatParticipatingUser> getAllParticipatingUserByChatroom(Chatroom chatroom);

    ChatParticipatingUser getChatroomAndUser(Chatroom chatroom, User user);
}
