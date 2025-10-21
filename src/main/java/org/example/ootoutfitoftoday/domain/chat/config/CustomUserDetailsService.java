package org.example.ootoutfitoftoday.domain.chat.config;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security의 UserDetailsService 인터페이스 구현 클래스.
 * <p>
 * JWT 토큰에서 추출된 사용자 ID(Subject)를 받아 데이터베이스에서
 * 실제 사용자 정보를 조회하고, 이를 Spring Security가 인식할 수 있는
 * UserDetails 객체(CustomUserDetails)로 변환하여 반환하는 역할을 수행합니다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // JWT의 Subject(사용자 ID)를 받아서 DB에서 사용자 정보를 로드합니다.
    @Override
    public UserDetails loadUserByUsername(String userIdString) throws UsernameNotFoundException {

        // 1. JWT ID는 Long 타입이라고 가정하고 변환
        Long userId = Long.parseLong(userIdString);

        // 2. UserRepository를 통해 DB에서 사용자 정보를 찾습니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userIdString));

        // 3. UserDetails 구현체를 반환합니다.
        return new CustomUserDetails(user);
    }
}
