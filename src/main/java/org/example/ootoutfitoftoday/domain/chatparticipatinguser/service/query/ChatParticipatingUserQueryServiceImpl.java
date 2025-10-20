package org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.repository.ChatParticipatingUserRepository;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatParticipatingUserQueryServiceImpl implements ChatParticipatingUserQueryService {

    private final ChatParticipatingUserRepository chatParticipatingUserRepository;

    @Override
    public List<ChatParticipatingUser> getChatParticipatingUsers(User user) {

        return chatParticipatingUserRepository.findByUserAndIsDeletedFalse(user);
    }

    @Override
    public List<ChatParticipatingUser> getAllParticipatingUserByChatroom(Chatroom chatroom) {

        return chatParticipatingUserRepository.findAllByChatroom(chatroom);
    }

    @Override
    public ChatParticipatingUser getChatroomAndUser(Chatroom chatroom, User user) {

        return chatParticipatingUserRepository.findByChatroomAndUser(chatroom, user);
    }
}
