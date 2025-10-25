package org.example.ootoutfitoftoday.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.domain.auth.enums.RefreshTokenStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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

    // 유저와 연관관계 없음(userId만 저장)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RefreshTokenStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private RefreshToken(
            Long userId,
            String token,
            RefreshTokenStatus status,
            LocalDateTime expiresAt
    ) {
        this.userId = userId;
        this.token = token;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    // 리프레시 토큰 생성
    public static RefreshToken create(
            Long userId,
            String token,
            LocalDateTime expiresAt
    ) {

        return RefreshToken.builder()
                .userId(userId)
                .token(token)
                .status(RefreshTokenStatus.ACTIVE)    // 기본값: ACTIVE
                .expiresAt(expiresAt)
                .build();
    }

    // 로그아웃 시 리프레시 토큰 무효화
    public void revoke() {

        this.status = RefreshTokenStatus.REVOKED;
    }

    // 리프레시 토큰 업데이트(RTR: Refresh Token Rotation)
    // 액세스 토큰 재발급 시, 리프레시 토큰도 갱신
    public void updateToken(String newToken, LocalDateTime newExpiresAt) {

        this.token = newToken;
        this.expiresAt = newExpiresAt;
        this.status = RefreshTokenStatus.ACTIVE;    // 갱신 후 상태를 ACTIVE로 복원
    }

    // 리프레시 토큰 만료 여부 확인
    // true = 현재 시간이 expiresAt 이후 즉 만료된 리프레시 토큰
    public boolean isExpired() {

        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // 리프레시 토큰 무효화 여부 확인
    // 로그아웃, 관리자 강제 만료 등으로 상태가 REVOKED인 경우
    public boolean isRevoked() {

        return this.status == RefreshTokenStatus.REVOKED;
    }

    // 리프레시 토큰 사용 가능 여부(만료X && 무효화X) 확인
    // 액세스 토큰 유효성과 별개. 리프레시 토큰만 판별
    public boolean isValid() {

        return !isExpired() && !isRevoked();
    }
}
