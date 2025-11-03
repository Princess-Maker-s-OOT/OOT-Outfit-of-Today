package org.example.ootoutfitoftoday.domain.chat.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chat.repository.ChatRepository;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatQueryServiceImpl implements ChatQueryService {

    private final ChatRepository chatRepository;
    private final ChatroomQueryService chatroomQueryService;

    // 채팅방 들어갈 시 조회되는 채팅 리스트
    @Override
    public Slice<ChatResponse> getChats(Long chatroomId, Pageable pageable) {
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
