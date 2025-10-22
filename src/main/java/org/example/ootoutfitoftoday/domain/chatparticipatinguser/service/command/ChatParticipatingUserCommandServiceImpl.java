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

    /**
     * 복합 키 생성 및 저장
     * 1. 해당 게시물의 id가 필요
     * 2. 게시물의 주인의 id가 필요
     */
    @Override
    public void saveKeys(Chatroom chatroom, SalePost salePost, User user) {
        // 거래 희망 유저(채팅하기 버튼을 누른 유저)
        // 복합키 생성
        ChatParticipatingUserId buyerId = ChatParticipatingUserId.create(chatroom.getId(), user.getId());
        // 중간테이블 엔티티 생성
        ChatParticipatingUser buyerParticipation = ChatParticipatingUser.create(buyerId, chatroom, user);

        // 게시물 주인(게시물을 작성한 유저)
        // 복합키 생성
        ChatParticipatingUserId sellerId = ChatParticipatingUserId.create(chatroom.getId(), salePost.getUser().getId());
        // 중간테이블 엔티티 생성
        ChatParticipatingUser sellerParticipation = ChatParticipatingUser.create(sellerId, chatroom, salePost.getUser());

        // 전체 저장
        chatParticipatingUserRepository.saveAll(java.util.List.of(buyerParticipation, sellerParticipation));
    }

    @Override
    public void softDeleteChatParticipatingUser(ChatParticipatingUser chatParticipatingUser) {
        chatParticipatingUser.softDelete();

        List<ChatParticipatingUser> chatParticipatingUsers = chatParticipatingUserRepository.findAllByChatroom(chatParticipatingUser.getChatroom());

        // 채팅방 내 다른 유저의 채팅방 소프트 딜리트가 true일 시 채팅방 내 채팅 일괄 삭제
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
