package org.example.ootoutfitoftoday.domain.user.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserGetResponse;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.exception.UserErrorCode;
import org.example.ootoutfitoftoday.domain.user.exception.UserException;
import org.example.ootoutfitoftoday.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean existsByLoginId(String loginId) {

        return userRepository.existsByLoginId(loginId);
    }

    @Override
    public boolean existsByEmail(String email) {

        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {

        return userRepository.existsByNickname(nickname);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {

        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public Optional<User> findByLoginIdAndIsDeletedFalse(String loginId) {

        return userRepository.findByLoginIdAndIsDeletedFalse(loginId);
    }

    @Override
    public Optional<User> findByIdAndIsDeletedFalse(Long id) {

        return userRepository.findByIdAndIsDeletedFalse(id);
    }

    public UserGetResponse getMyProfile(Long userId) {

        User user = userRepository.findByIdAndIsDeletedFalse(userId).orElseThrow(
                () -> new UserException(UserErrorCode.USER_NOT_FOUND)
        );

        return UserGetResponse.from(user);
    }
}
