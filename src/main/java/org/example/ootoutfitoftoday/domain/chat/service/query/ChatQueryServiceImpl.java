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
public class ChatQueryServiceImpl implements ChatQueryService {

    private final ChatRepository chatRepository;

    @Override
    public Chat getFinalChat(Chatroom chatroom) {

        return chatRepository.findFirstByChatroomAndDeletedIsFalseOrderByCreatedAtDesc(chatroom).orElse(null);
    }

    @Override
    public int getCountNotReadChat(Chatroom chatroom) {

        return chatRepository.countByChatroomAndDeletedIsFalseAndReadedIsFalse(chatroom);
    }
}
