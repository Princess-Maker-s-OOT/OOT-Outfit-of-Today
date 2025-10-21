package org.example.ootoutfitoftoday.domain.chat.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chat.dto.response.ChatResponse;
import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chat.repository.ChatRepository;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatQueryServiceImpl implements ChatQueryService {

    private final ChatRepository chatRepository;
    private final ChatroomQueryService chatroomQueryService;

    // 채팅방 들어갈 시 조회되는 채팅 리스트
    @Override
    public Slice<ChatResponse> getChats(Long chatroomId, Long userId, Pageable pageable) {
        Chatroom chatroom = chatroomQueryService.getChatroomById(chatroomId);

        List<Chat> Chats = chatRepository.findByChatroomAndIsDeletedFalseOrderByCreatedAtDesc(chatroom);

        List<ChatResponse> chatResponses = Chats.stream()
                .map(chat -> ChatResponse.of(
                        chat.getId(),
                        chat.getChatroom().getId(),
                        chat.getUser().getId(),
                        chat.getContent(),
                        chat.getCreatedAt()
                ))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), chatResponses.size());
        List<ChatResponse> subList = (start >= chatResponses.size()) ? List.of() : chatResponses.subList(start, end);
        boolean hasNext = end < chatResponses.size();

        return new SliceImpl<>(subList, pageable, hasNext);
    }
}
