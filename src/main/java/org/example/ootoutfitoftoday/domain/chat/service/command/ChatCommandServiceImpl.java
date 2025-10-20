package org.example.ootoutfitoftoday.domain.chat.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chat.repository.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatCommandServiceImpl implements ChatCommandService {

    private final ChatRepository chatRepository;
}
