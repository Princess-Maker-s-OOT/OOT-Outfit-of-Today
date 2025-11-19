package org.example.ootoutfitoftoday.domain.chat.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chat.entity.Chat;
import org.example.ootoutfitoftoday.domain.chat.repository.ChatRepository;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatReferenceToChatroomQueryServiceImpl implements ChatReferenceToChatroomQueryService {

    private final ChatRepository chatRepository;

    @Override
    public Chat getFinalChat(Chatroom chatroom) {

        return chatRepository.findFirstByChatroomAndIsDeletedFalseOrderByCreatedAtDesc(chatroom).orElse(null);
    }
}
