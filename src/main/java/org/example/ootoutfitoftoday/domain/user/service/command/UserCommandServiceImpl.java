package org.example.ootoutfitoftoday.domain.user.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.exception.UserErrorCode;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
import org.example.ootoutfitoftoday.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;

    @Override
    public void save(User user) {

        userRepository.save(user);
    }

    @Override
    public void softDeleteUser(User user) {

        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.USER_ALREADY_WITHDRAWN);
        }

        user.softDelete();
        userRepository.save(user);
    }
}
