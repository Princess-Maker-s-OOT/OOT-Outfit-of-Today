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
@Table(
        name = "refresh_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"})
)
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


    // 디바이스 고유 식별자(클라이언트에서 생성한 UUID)
    @Column(name = "device_id", nullable = false, length = 255)
    private String deviceId;

    // 디바이스 명
    @Column(name = "device_name", length = 100)
    private String deviceName;

    // 유니크 제거 조건 제거 -> 디바이스별로 여러 토큰 허용
    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // 마지막 사용 시간(디바이스 활동 추적)
    // 토큰 갱신 시
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // IP 주소(보안 추적)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // 브라우저 & 디바이스 정보
    // HTTP 요청 헤더 중 하나로, 클라이언트 환경 정보를 문자열로 전달
    // 서버에서는 누가 어떤 환경에서 요청했는지 확인 가능
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Builder(access = AccessLevel.PROTECTED)
    private RefreshToken(
            User user,
            String deviceId,
            String deviceName,
            String token,
            LocalDateTime expiresAt,
            LocalDateTime lastUsedAt,
            String ipAddress,
            String userAgent
    ) {
        this.user = user;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.token = token;
        this.expiresAt = expiresAt;
        this.lastUsedAt = lastUsedAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // 리프레시 토큰 생성
    public static RefreshToken create(
            User user,
            String deviceId,
            String deviceName,
            String token,
            LocalDateTime expiresAt,
            String ipAddress,
            String userAgent
    ) {

        return RefreshToken.builder()
                .user(user)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .token(token)
                .expiresAt(expiresAt)
                .lastUsedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    // 리프레시 토큰 업데이트(RTR: Refresh Token Rotation)
    // 액세스 토큰 재발급 시, 리프레시 토큰도 갱신
    public void updateToken(String newToken, LocalDateTime newExpiresAt) {

        this.token = newToken;
        this.expiresAt = newExpiresAt;
        this.lastUsedAt = LocalDateTime.now();    // 업데이트 시 마지막 사용 시간도 갱신
    }

    // 리프레시 토큰 만료 여부 확인
    // true = 현재 시간이 expiresAt 이후 즉 만료된 리프레시 토큰
    public boolean isExpired(LocalDateTime now) {

        return now.isAfter(this.expiresAt);
    }

    // 리프레시 토큰 사용 가능 여부(만료X) 확인
    // 액세스 토큰 유효성과 별개. 리프레시 토큰만 판별
    public boolean isValid(LocalDateTime now) {

        return !isExpired(now);
    }
}
