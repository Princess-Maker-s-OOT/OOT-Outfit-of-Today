package org.example.ootoutfitoftoday.domain.chatroom.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.command.ChatParticipatingUserCommandService;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query.ChatParticipatingUserQueryService;
import org.example.ootoutfitoftoday.domain.chatroom.dto.request.ChatroomRequest;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.chatroom.exception.ChatroomErrorCode;
import org.example.ootoutfitoftoday.domain.chatroom.exception.ChatroomException;
import org.example.ootoutfitoftoday.domain.chatroom.repository.ChatroomRepository;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.service.query.SalePostQueryService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatroomCommandServiceImpl implements ChatroomCommandService {

    private final ChatroomRepository chatroomRepository;
    private final SalePostQueryService salePostQueryService;
    private final UserQueryService userQueryService;
    private final ChatParticipatingUserCommandService chatParticipatingUserCommandService;
    private final ChatParticipatingUserQueryService chatParticipatingUserQueryService;

    // 채팅방 생성
    @Override
    public void createChatroom(ChatroomRequest chatroomRequest, Long userId) {
        log.info("ChatroomService.createChatroom : userId={} 채팅방 생성", userId);

        Long salePostId = chatroomRequest.salePostId();

        SalePost salePost = salePostQueryService.findSalePostById(salePostId);

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        if (Objects.equals(salePost.getUser(), user)) {
            throw new ChatroomException(ChatroomErrorCode.EQUAL_SELLER_BUYER);
        }

        Chatroom chatroom = Chatroom.create(salePost);

        Chatroom saveChatroom = chatroomRepository.save(chatroom);

        saveChatroom.addChatParticipatingUser(salePost.getUser());

        saveChatroom.addChatParticipatingUser(user);

        chatParticipatingUserCommandService.saveKeys(saveChatroom, salePost, user);
    }

    // 채팅방 삭제
    @Override
    public void deleteChatroom(Long chatroomId, Long userId) {
        log.info("ChatroomService.deleteChatroom : userId={} 채팅방 삭제", userId);

        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow(
                () -> new ChatroomException(ChatroomErrorCode.NOT_EXIST_CHATROOM)
        );

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        ChatParticipatingUser chatParticipatingUser = chatParticipatingUserQueryService.getChatroomAndUser(chatroom, user);

        chatParticipatingUserCommandService.softDeleteChatParticipatingUser(chatParticipatingUser);
    }
}
