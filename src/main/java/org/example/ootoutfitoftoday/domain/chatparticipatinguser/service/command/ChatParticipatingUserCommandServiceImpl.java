package org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.chat.service.command.ChatReferenceToChatroomCommandService;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUserId;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.repository.ChatParticipatingUserRepository;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatParticipatingUserCommandServiceImpl implements ChatParticipatingUserCommandService {

    private final ChatParticipatingUserRepository chatParticipatingUserRepository;
    private final ChatReferenceToChatroomCommandService chatReferenceToChatroomCommandService;

    @Override
    public void saveKeys(Chatroom chatroom, SalePost salePost, User user) {
        ChatParticipatingUserId buyerId = ChatParticipatingUserId.create(chatroom.getId(), user.getId());

        ChatParticipatingUser buyerParticipation = ChatParticipatingUser.create(buyerId, chatroom, user);

        ChatParticipatingUserId sellerId = ChatParticipatingUserId.create(chatroom.getId(), salePost.getUser().getId());

        ChatParticipatingUser sellerParticipation = ChatParticipatingUser.create(sellerId, chatroom, salePost.getUser());

        chatParticipatingUserRepository.saveAll(java.util.List.of(buyerParticipation, sellerParticipation));
    }

    @Override
    public void softDeleteChatParticipatingUser(ChatParticipatingUser chatParticipatingUser) {
        chatParticipatingUser.softDelete();

        List<ChatParticipatingUser> chatParticipatingUsers = chatParticipatingUserRepository.findAllByChatroom(chatParticipatingUser.getChatroom());

        chatParticipatingUsers
                .forEach(chatParticipatingUser1 -> {
                    if (!Objects.equals(chatParticipatingUser1.getUser().getId(), chatParticipatingUser.getUser().getId()) &&
                            chatParticipatingUser1.isDeleted()
                    ) {
                        log.info("채팅방 삭제 {}", chatParticipatingUser1.getId());
                        chatReferenceToChatroomCommandService.deleteChats(chatParticipatingUser1.getChatroom().getId());
                    }
                });
    }
}
