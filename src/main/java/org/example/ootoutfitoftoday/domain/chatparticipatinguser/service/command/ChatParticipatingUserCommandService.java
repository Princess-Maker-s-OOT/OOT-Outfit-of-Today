package org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.command;

import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.user.entity.User;

public interface ChatParticipatingUserCommandService {

    // 하나의 채팅방 id와 채팅방에 해당하는 각 유저들의 id를 복합키로 사용하기 위해 저장하는 메서드
    void saveKeys(Chatroom chatroom, SalePost salePost, User user);

    // ChatParticipatingUser 소프트 딜리트
    void softDeleteChatParticipatingUser(ChatParticipatingUser chatParticipatingUser);
}
