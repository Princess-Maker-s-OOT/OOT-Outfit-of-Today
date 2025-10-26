package org.example.ootoutfitoftoday.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder(access = AccessLevel.PROTECTED)
    private RefreshToken(
            User user,
            String token,
            LocalDateTime expiresAt
    ) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    // 리프레시 토큰 생성
    public static RefreshToken create(
            User user,
            String token,
            LocalDateTime expiresAt
    ) {

        return RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .build();
    }

    // 리프레시 토큰 업데이트(RTR: Refresh Token Rotation)
    // 액세스 토큰 재발급 시, 리프레시 토큰도 갱신
    public void updateToken(String newToken, LocalDateTime newExpiresAt) {

        this.token = newToken;
        this.expiresAt = newExpiresAt;
    }

    // 리프레시 토큰 만료 여부 확인
    // true = 현재 시간이 expiresAt 이후 즉 만료된 리프레시 토큰
    public boolean isExpired() {

        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // 리프레시 토큰 사용 가능 여부(만료X) 확인
    // 액세스 토큰 유효성과 별개. 리프레시 토큰만 판별
    public boolean isValid() {

        return !isExpired();
    }
}
