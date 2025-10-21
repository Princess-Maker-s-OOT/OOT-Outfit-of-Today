package org.example.ootoutfitoftoday.domain.chat.config;

import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security의 UserDetails 인터페이스 구현 클래스.
 * * 데이터베이스의 User 엔티티를 래핑하여 Spring Security가 필요로 하는
 * 사용자 ID, 비밀번호, 권한 등의 인증 및 인가 정보를 제공하는 어댑터 역할을 수행합니다.
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * 인증된 사용자 ID를 직접 얻고 싶을 때 사용
     *
     * @return 사용자의 Long 타입 ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * 사용자가 가진 권한 목록을 반환합니다.
     *
     * @return 권한 목록 (GrantedAuthority)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // User 엔티티의 getRole() 메서드가 UserRole enum을 반환한다고 가정하며,
        // ROLE_USER, ROLE_ADMIN과 같은 형식으로 변환합니다.
        String roleName = user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    /**
     * 사용자의 비밀번호를 반환합니다.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 계정 고유 식별자(JWT에서는 보통 ID를 사용)를 반환합니다.
     */
    @Override
    public String getUsername() {
        return user.getId().toString();
    }

    // --- 이하 계정 상태 관련 설정은 모두 true로 설정합니다 ---

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
