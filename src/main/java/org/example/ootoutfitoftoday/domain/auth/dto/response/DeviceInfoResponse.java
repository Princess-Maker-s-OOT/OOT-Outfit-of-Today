package org.example.ootoutfitoftoday.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeviceInfoResponse {

    private String deviceId;
    private String deviceName;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
    private boolean isCurrent;
    private String ipAddress;
    private String userAgent;
}
