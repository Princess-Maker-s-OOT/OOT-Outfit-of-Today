package org.example.ootoutfitoftoday.domain.user.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.repository.UserRepository;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public void initializeAdmin(
            String loginId,
            String email,
            String nickname,
            String username,
            String password,
            String phoneNumber
    ) {
        if (!userQueryService.existsByLoginId(loginId)) {
            User admin = User.createAdmin(
                    loginId,
                    email,
                    nickname,
                    username,
                    passwordEncoder.encode(password),
                    phoneNumber
            );
            save(admin);
            System.out.println("관리자 계정 초기 생성 완료되었습니다.");
        } else {
            System.out.println("관리자 계정이 이미 존재합니다.");
        }
    }
}
