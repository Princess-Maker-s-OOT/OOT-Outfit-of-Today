package org.example.ootoutfitoftoday.domain.chat.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chat.dto.request.ChatRequest;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chat.repository.ChatRepository;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.command.ChatParticipatingUserCommandService;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query.ChatParticipatingUserQueryService;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatCommandServiceImpl implements ChatCommandService {

    private final ChatRepository chatRepository;
    private final ChatroomQueryService chatroomQueryService;
    private final UserQueryService userQueryService;
    private final ChatParticipatingUserQueryService chatParticipatingUserQueryService;
    private final ChatParticipatingUserCommandService chatParticipatingUserCommandService;

    @Override
    public ChatResponse createChat(ChatRequest chatRequest, Long chatroomId, Long userId) {
        Chatroom chatroom = chatroomQueryService.getChatroomById(chatroomId);
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);
        Chat chat = Chat.create(
                chatRequest.content(),
                chatroom,
                user
        );

        Chat savedChat = chatRepository.save(chat);

        List<ChatParticipatingUser> chatParticipatingUsers = chatParticipatingUserQueryService.getAllParticipatingUserByChatroom(chatroom);

        chatParticipatingUsers.forEach(chatParticipatingUser -> {
            // 1. 상대방 정보를 찾고
            if (!Objects.equals(chatParticipatingUser.getUser(), user) &&
                    // 2. 상대방의 user.isDeleted()가 false인가? user.isDeleted()가 true라면 패스
                    !chatParticipatingUser.getUser().isDeleted() &&
                    // 3. 상대방이 채팅방을 삭제했는가? 했다면
                    chatParticipatingUser.isDeleted()
            ) {
                // 4. 상대 채팅방 isDeleted true로 수정
                chatParticipatingUser.restore();
            }
        });

        return ChatResponse.of(
                savedChat.getChatroom().getId(),
                savedChat.getUser().getId(),
                savedChat.getUser().getNickname(),
                savedChat.getId(),
                savedChat.getContent(),
                savedChat.getCreatedAt()
        );
    }

    @Override
    public void deleteChats(Long chatroomId) {
        chatRepository.bulkSoftDeleteChatData(chatroomId, LocalDateTime.now());
    }
}
