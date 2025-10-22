package org.example.ootoutfitoftoday.domain.chat.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chat.dto.request.ChatRequest;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chat.repository.ChatRepository;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatCommandServiceImpl implements ChatCommandService {

    private final ChatRepository chatRepository;
    private final ChatroomQueryService chatroomQueryService;
    private final UserQueryService userQueryService;

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
