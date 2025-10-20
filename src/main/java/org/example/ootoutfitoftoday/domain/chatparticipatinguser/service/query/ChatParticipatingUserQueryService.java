package org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query;

import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.util.List;

public interface ChatParticipatingUserQueryService {

    // 유저를 통해서 전체 채팅방에 대한 객체를 얻기 위한 메서드
    List<ChatParticipatingUser> getChatParticipatingUsers(User user);

    // 채팅방을 통해 참여중인 User 객체들을 얻기 위한 메서드
    List<ChatParticipatingUser> getAllParticipatingUserByChatroom(Chatroom chatroom);

    // 채팅방, 유저를 통해 중간 테이블 데이터 얻기 위한 메서드
    ChatParticipatingUser getChatroomAndUser(Chatroom chatroom, User user);
}
