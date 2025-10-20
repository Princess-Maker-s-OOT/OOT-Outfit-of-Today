package org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.repository.ChatParticipatingUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatParticipatingUserQueryServiceImpl implements ChatParticipatingUserQueryService {

    private final ChatParticipatingUserRepository chatParticipatingUserRepository;
}
