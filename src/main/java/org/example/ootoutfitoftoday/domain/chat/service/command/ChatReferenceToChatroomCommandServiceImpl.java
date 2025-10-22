package org.example.ootoutfitoftoday.domain.chat.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chat.repository.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatReferenceToChatroomCommandServiceImpl implements ChatReferenceToChatroomCommandService {

    private final ChatRepository chatRepository;

    @Override
    public void deleteChats(Long chatroomId) {

        chatRepository.bulkSoftDeleteChatData(chatroomId, LocalDateTime.now());
    }
}
