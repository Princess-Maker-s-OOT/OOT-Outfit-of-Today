package org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.command;

import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.user.entity.User;

public interface ChatParticipatingUserCommandService {

    void saveKeys(Chatroom chatroom, SalePost salePost, User user);

    void softDeleteChatParticipatingUser(ChatParticipatingUser chatParticipatingUser);
}
