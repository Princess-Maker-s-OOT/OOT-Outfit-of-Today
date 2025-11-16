package org.example.ootoutfitoftoday.domain.chat.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chat.repository.ChatRepository;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatQueryServiceImpl implements ChatQueryService {

    private final ChatRepository chatRepository;
    private final ChatroomQueryService chatroomQueryService;

    @Override
    public Slice<ChatResponse> getChats(Long chatroomId, Pageable pageable) {
        log.info("[GET] /v1/chatrooms/{chatroomId}/chats : Service 작동");

        Chatroom chatroom = chatroomQueryService.getChatroomById(chatroomId);

        Slice<Chat> chats = chatRepository.findByChatroomAndIsDeletedFalseOrderByCreatedAtDesc(chatroom, pageable);

        return chats.map(chat -> ChatResponse.of(
                chat.getChatroom().getId(),
                (chat.getUser().getId() != null) ? chat.getUser().getId() : null,
                (chat.getUser().getNickname() != null) ? chat.getUser().getNickname() : null,
                chat.getId(),
                chat.getContent(),
                chat.getCreatedAt()
        ));
    }

    @Override
    public boolean existsByChatroom(Long chatroomId) {
        return chatRepository.existsByChatroomIdAndIsDeletedFalse(chatroomId);
    }
}
