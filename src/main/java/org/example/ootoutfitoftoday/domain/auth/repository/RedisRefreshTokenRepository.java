package org.example.ootoutfitoftoday.domain.auth.repository;

import org.example.ootoutfitoftoday.domain.auth.dto.response.DeviceInfoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Redis 기반 리프레시 토큰 저장소 인터페이스
 * - Sorted Set을 이용한 토큰 관리
 * - Score: 만료 시간(타임스탬프)
 * - Member: 토큰 값
 */
public interface RedisRefreshTokenRepository {

    /**
     * 리프레시 토큰 저장
     *
     * @param userId     사용자 ID
     * @param deviceId   디바이스 ID
     * @param deviceName 디바이스 명
     * @param token      리프레시 토큰
     * @param expiresAt  만료 시간
     * @param ipAddress  IP 주소
     * @param userAgent  User-Agent
     */
    void save(
            Long userId,
            String deviceId,
            String deviceName,
            String token,
            LocalDateTime expiresAt,
            String ipAddress,
            String userAgent
    );

    // 토큰으로 사용자 ID 조회
    Optional<Long> findUserIdByToken(String token);

    // 토큰으로 디바이스 ID 조회
    Optional<String> findDeviceIdByToken(String token);

    // 사용자의 특정 디바이스 토큰 조회
    Optional<String> findByUserIdAndDeviceId(Long userId, String deviceId);

    // 토큰 메타데이터 조회
    Optional<DeviceInfoResponse> findTokenMetadata(String token);

    // 사용자의 모든 디바이스 목록 조회(최근 사용순)
    List<DeviceInfoResponse> findAllByUserId(Long userId);

    // 사용자의 디바이스 수 조회
    long countByUserId(Long userId);

    // 특정 사용자의 가장 오래된 디바이스 조회
    Optional<DeviceInfoResponse> findOldestDeviceByUserId(Long userId);

    /**
     * 토큰 업데이트(RTR)
     *
     * @param userId       사용자 ID
     * @param deviceId     디바이스 ID
     * @param oldToken     기존 토큰
     * @param newToken     새 토큰
     * @param newExpiresAt 새 만료 시간
     * @param ipAddress    IP 주소
     * @param userAgent    User-Agent
     */
    void updateToken(
            Long userId,
            String deviceId,
            String oldToken,
            String newToken,
            LocalDateTime newExpiresAt,
            String ipAddress,
            String userAgent
    );

    // 특정 디바이스 삭제
    void deleteByUserIdAndDeviceId(Long userId, String deviceId);

    // 사용자의 모든 디바이스 삭제
    void deleteAllByUserId(Long userId);

    // 만료된 토큰 정리
    long deleteExpiredTokens();

    // 토큰 존재 여부 확인
    boolean existsByToken(String token);
}