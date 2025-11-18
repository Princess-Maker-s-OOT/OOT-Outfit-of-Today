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

    @Column(name = "device_id", nullable = false, length = 255)
    private String deviceId;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

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

    public void updateToken(
            String newToken,
            LocalDateTime newExpiresAt,
            String ipAddress,
            String userAgent
    ) {
        this.token = newToken;
        this.expiresAt = newExpiresAt;
        this.lastUsedAt = LocalDateTime.now();
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public boolean isExpired(LocalDateTime now) {

        return now.isAfter(this.expiresAt);
    }

    public boolean isValid(LocalDateTime now) {

        return !isExpired(now);
    }
}
