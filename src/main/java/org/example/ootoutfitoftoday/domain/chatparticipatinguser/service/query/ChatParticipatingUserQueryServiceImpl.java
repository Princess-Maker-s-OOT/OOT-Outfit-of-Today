package org.example.ootoutfitoftoday.domain.chatparticipatinguser.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.ChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.repository.ChatParticipatingUserRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatParticipatingUserQueryServiceImpl implements ChatParticipatingUserQueryService {

    private final ChatParticipatingUserRepository chatParticipatingUserRepository;
    private final UserQueryService userQueryService;

    @Override
    public List<ChatParticipatingUser> getChatParticipatingUsers(User user) {

        return chatParticipatingUserRepository.findByUserAndDeletedIsFalse(user);
    }
}
