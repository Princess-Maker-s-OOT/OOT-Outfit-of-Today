package org.example.ootoutfitoftoday.domain.auth.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
// 디바이스 목록 조회용 응답 DTO
public class DeviceInfoResponse {

    // 디바이스 고유 ID
    private String deviceId;

    // 디바이스 명
    private String deviceName;

    // 마지막 사용 시간
    private LocalDateTime lastUsedAt;

    // 토큰 만료 시간
    private LocalDateTime expiresAt;

    // 현재 요청한 디바이스인지 여부
    private boolean isCurrent;

    // IP 주소
    private String ipAddress;

    // 브라우조 & 디바이스 정보
    private String userAgent;
}
