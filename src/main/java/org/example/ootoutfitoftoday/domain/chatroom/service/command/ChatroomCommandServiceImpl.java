package org.example.ootoutfitoftoday.domain.chatroom.service.command;

import lombok.RequiredArgsConstructor;
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
        Long salePostId = chatroomRequest.salePostId();

        // 게시판 주인을 찾기 위한 게시판 찾기
        SalePost salePost = salePostQueryService.findSalePostById(salePostId);
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        // 판매자와 구매자가 일치하는 경우
        if (Objects.equals(salePost.getUser(), user)) {
            // 예외 처리
            throw new ChatroomException(ChatroomErrorCode.EQUAL_SELLER_BUYER);
        }

        Chatroom chatroom = Chatroom.create(salePost);

        Chatroom saveChatroom = chatroomRepository.save(chatroom);

        // OneToMany 필드에 데이터 삽입
        // 1. 채팅방 - 판매자
        saveChatroom.addChatParticipatingUser(salePost.getUser());
        // 2. 채팅방 - 구매자
        saveChatroom.addChatParticipatingUser(user);

        chatParticipatingUserCommandService.saveKeys(saveChatroom, salePost, user);
    }

    // 채팅방 삭제
    @Override
    public void deleteChatroom(Long chatroomId, Long userId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow(
                () -> new ChatroomException(ChatroomErrorCode.NOT_EXIST_CHATROOM)
        );
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        ChatParticipatingUser chatParticipatingUser = chatParticipatingUserQueryService.getChatroomAndUser(chatroom, user);

        chatParticipatingUserCommandService.softDeleteChatParticipatingUser(chatParticipatingUser);
    }
}
