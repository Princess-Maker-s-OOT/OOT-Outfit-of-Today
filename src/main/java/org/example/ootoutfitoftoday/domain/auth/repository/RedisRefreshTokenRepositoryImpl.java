package org.example.ootoutfitoftoday.domain.auth.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.dto.response.DeviceInfoResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis Sorted Set 기반 리프레시 토큰 저장소 구현
 * 저장 구조:
 * 1. Sorted Set(정방향 인덱스)
 * - Key: "refresh_tokens:user:{userId}"
 * - Member: {token}
 * - Score: 만료시간 타임스탬프 (초단위)
 * 2. Hash(역인덱스 - 토큰 -> 사용자 정보)
 * - Key: "token_to_user:{token}"
 * - Fields: userId, deviceId
 * 3. Hash(디바이스 메타데이터) - MySQL RefreshToken 엔티티 필드와 동일
 * - Key: "device_meta:{userId}:{deviceId}"
 * - Fields: userId, deviceId, deviceName, token, expiresAt, lastUsedAt, ipAddress, userAgent
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepositoryImpl implements RedisRefreshTokenRepository {

    // Redis 키 접두사
    private static final String USER_TOKENS_PREFIX = "refresh_tokens:user:";    // Sorted Set
    private static final String TOKEN_TO_USER_PREFIX = "token_to_user:";        // Hash(역인덱스)
    private static final String DEVICE_META_PREFIX = "device_meta:";            // Hash(메타데이터)

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 리프레시 토큰 저장
     * 3가지 데이터 구조에 저장:
     * 1. Sorted Set: userId -> tokens(만료시간 기준 정렬)
     * 2. Hash: token -> userId, deviceId(역인덱스)
     * 3. Hash: 디바이스 메타데이터
     * 파라미터 순서는 기존의 MySQL RefreshToken 엔티티 필드 순서와 동일
     */
    @Override
    public void save(
            Long userId,
            String deviceId,
            String deviceName,
            String token,
            LocalDateTime expiresAt,
            String ipAddress,
            String userAgent
    ) {
        // 1. Sorted Set에 토큰 저장(score = 만료시간)
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        double score = toTimestamp(expiresAt);
        redisTemplate.opsForZSet().add(userTokensKey, token, score);

        // TTL 설정(가장 긴 토큰 만료시간 + 1일 여유)
        long ttlSeconds = calculateTtl(expiresAt);
        redisTemplate.expire(userTokensKey, ttlSeconds, TimeUnit.SECONDS);

        // 역인덱스 저장(token -> userId, deviceId)
        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
        Map<String, String> tokenMapping = new HashMap<>();
        tokenMapping.put("userId", userId.toString());
        tokenMapping.put("deviceId", deviceId);
        redisTemplate.opsForHash().putAll(tokenToUserKey, tokenMapping);
        redisTemplate.expire(tokenToUserKey, ttlSeconds, TimeUnit.SECONDS);

        // 디바이스 메타데이터 저장(기존 MySQL 엔티티 필드 순서와 일치)
        String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
        Map<String, String> metadata = new LinkedHashMap<>();    // 순서 보장

        // MySQL RefreshToken 엔티티 필드 순서와 동일하게 저장
        metadata.put("userId", userId.toString());
        metadata.put("deviceId", deviceId);
        metadata.put("deviceName", deviceName != null ? deviceName : "Unknown Device");
        metadata.put("token", token);
        metadata.put("expiresAt", expiresAt.toString());
        metadata.put("lastUsedAt", LocalDateTime.now().toString());
        metadata.put("ipAddress", ipAddress != null ? ipAddress : "");
        metadata.put("userAgent", userAgent != null ? userAgent : "");

        redisTemplate.opsForHash().putAll(deviceMetaKey, metadata);
        redisTemplate.expire(deviceMetaKey, ttlSeconds, TimeUnit.SECONDS);

        log.debug("Redis에 토큰 저장 완료 - userId: {}, deviceId: {}", userId, deviceId);
    }

    // 토큰으로 사용자 ID 조회(역인덱스 사용)
    @Override
    public Optional<Long> findUserIdByToken(String token) {

        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
        Object userIdObj = redisTemplate.opsForHash().get(tokenToUserKey, "userId");    // TODO: 타입을 오브젝트로...?

        if (userIdObj == null) {

            return Optional.empty();
        }

        return Optional.of(Long.parseLong(userIdObj.toString()));
    }

    // 토큰으로 디바이스 ID 조회(역인덱스 사용)
    @Override
    public Optional<String> findDeviceIdByToken(String token) {

        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
        Object deviceIdObj = redisTemplate.opsForHash().get(tokenToUserKey, "deviceId");

        return Optional.ofNullable(deviceIdObj != null ? deviceIdObj.toString() : null);
    }

    // 사용자의 특정 디바이스 토큰 조회
    @Override
    public Optional<String> findByUserIdAndDeviceId(Long userId, String deviceId) {

        String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
        Object tokenObj = redisTemplate.opsForHash().get(deviceMetaKey, "token");

        return Optional.ofNullable(tokenObj != null ? tokenObj.toString() : null);
    }

    // 토큰 메타데이터 조회
    @Override
    public Optional<DeviceInfoResponse> findTokenMetadata(String token) {

        // 역인덱스로 userId, deviceId 조회
        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
        Map<Object, Object> tokenInfo = redisTemplate.opsForHash().entries(tokenToUserKey);

        if (tokenInfo.isEmpty()) {

            return Optional.empty();
        }

        Long userId = Long.parseLong(tokenInfo.get("userId").toString());
        String deviceId = tokenInfo.get("deviceId").toString();

        // 디바이스 메타데이터 조회
        String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
        Map<Object, Object> metadata = redisTemplate.opsForHash().entries(deviceMetaKey);

        if (metadata.isEmpty()) {

            return Optional.empty();
        }

        return Optional.of(buildDeviceInfoResponse(deviceId, metadata, null));
    }

    // 사용자의 모든 디바이스 목록 조회(최근 사용순)
    @Override
    public List<DeviceInfoResponse> findAllByUserId(Long userId) {

        String userTokensKey = USER_TOKENS_PREFIX + userId;

        // Sorted Set에서 모든 토큰 조회 (score 기준 오름차순)
        Set<String> tokens = redisTemplate.opsForZSet().range(userTokensKey, 0, -1);

        if (tokens == null || tokens.isEmpty()) {

            return Collections.emptyList();
        }

        List<DeviceInfoResponse> devices = new ArrayList<>();

        for (String token : tokens) {
            // 역인덱스로 deviceId 조회
            Optional<String> deviceIdOpt = findDeviceIdByToken(token);

            if (deviceIdOpt.isEmpty()) {
                continue;
            }

            String deviceId = deviceIdOpt.get();
            String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
            Map<Object, Object> metadata = redisTemplate.opsForHash().entries(deviceMetaKey);

            if (!metadata.isEmpty()) {
                devices.add(buildDeviceInfoResponse(deviceId, metadata, null));
            }
        }

        // lastUsedAt 기준 내림차순 정렬(최근 사용 순)
        devices.sort((d1, d2) -> d2.getLastUsedAt().compareTo(d1.getLastUsedAt()));

        return devices;
    }

    // 사용자의 디바이스 수 조회
    @Override
    public long countByUserId(Long userId) {

        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Long count = redisTemplate.opsForZSet().zCard(userTokensKey);

        return count != null ? count : 0L;
    }

    // 가장 오래된 디바이스 조회(lastUsedAt 기준)
    @Override
    public Optional<DeviceInfoResponse> findOldestDeviceByUserId(Long userId) {

        List<DeviceInfoResponse> devices = findAllByUserId(userId);

        if (devices.isEmpty()) {

            return Optional.empty();
        }

        DeviceInfoResponse oldestDevice = null;

        for (DeviceInfoResponse device : devices) {
            if (oldestDevice == null || device.getLastUsedAt().isBefore(oldestDevice.getLastUsedAt())) {
                oldestDevice = device;
            }
        }

        // Optional로 반환
        return Optional.ofNullable(oldestDevice);
    }

    // 토큰 업데이트(RTR - Refresh Token Rotation)
    // 기존 토큰 삭제 후 새 토큰 저장
    @Override
    public void updateToken(
            Long userId,
            String deviceId,
            String oldToken,
            String newToken,
            LocalDateTime newExpiresAt,
            String ipAddress,
            String userAgent
    ) {
        // 기존 메타데이터에서 deviceName 가져오기
        String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
        Object deviceNameObj = redisTemplate.opsForHash().get(deviceMetaKey, "deviceName");
        String deviceName = deviceNameObj != null ? deviceNameObj.toString() : "Unknown Device";

        // 기존 토큰 삭제
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        redisTemplate.opsForZSet().remove(userTokensKey, oldToken);

        String oldTokenToUserKey = TOKEN_TO_USER_PREFIX + oldToken;
        redisTemplate.delete(oldTokenToUserKey);

        // 새 토큰 저장(save 메서드 재사용)
        save(userId, deviceId, deviceName, newToken, newExpiresAt, ipAddress, userAgent);

        log.debug("토큰 업데이트 완료 - userId: {}, deviceId: {}", userId, deviceId);
    }

    // 특정 디바이스 삭제
    @Override
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        // 토큰 조회
        Optional<String> tokenOpt = findByUserIdAndDeviceId(userId, deviceId);

        if (tokenOpt.isEmpty()) {
            log.warn("삭제할 토큰을 찾을 수 없음 - userId: {}, deviceId: {}", userId, deviceId);

            return;
        }

        String token = tokenOpt.get();

        // 1. Sorted Set에서 토큰 삭제
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        redisTemplate.opsForZSet().remove(userTokensKey, token);

        // 2. 역인덱스 삭제
        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
        redisTemplate.delete(tokenToUserKey);

        // 3. 디바이스 메타데이터 삭제
        String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
        redisTemplate.delete(deviceMetaKey);

        log.info("디바이스 삭제 완료 - userId: {}, deviceId: {}", userId, deviceId);
    }

    // 사용자의 모든 디바이스 삭제
    @Override
    public void deleteAllByUserId(Long userId) {

        String userTokensKey = USER_TOKENS_PREFIX + userId;

        // 모든 토큰 조회
        Set<String> tokens = redisTemplate.opsForZSet().range(userTokensKey, 0, -1);

        if (tokens == null || tokens.isEmpty()) {

            return;
        }

        // 각 토큰에 대한 역인덱스와 메타데이터 삭제
        for (String token : tokens) {

            Optional<String> deviceIdOpt = findDeviceIdByToken(token);

            // 역인덱스 삭제
            String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
            redisTemplate.delete(tokenToUserKey);

            // 메타데이터 삭제
            if (deviceIdOpt.isPresent()) {
                String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceIdOpt.get();
                redisTemplate.delete(deviceMetaKey);
            }
        }

        // Sorted Set 삭제
        redisTemplate.delete(userTokensKey);

        log.info("사용자의 모든 디바이스 삭제 완료 - userId: {}, 삭제된 토큰 수: {}", userId, tokens.size());
    }

    // 만료된 토큰 정리
    // Sorted Set의 score(만료시간)를 기준으로 현재 시간 이전 토큰 삭제
    @Override
    public long deleteExpiredTokens() {

        long totalDeleted = 0;
        double currentTimestamp = toTimestamp(LocalDateTime.now());

        // 모든 사용자의 토큰 키 패턴 조회
        Set<String> keys = redisTemplate.keys(USER_TOKENS_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {

            return 0;
        }

        for (String key : keys) {
            // 현재 시간보다 이전 score를 가진 토큰 조회
            Set<String> expiredTokens = redisTemplate.opsForZSet().rangeByScore(key, 0, currentTimestamp);

            if (expiredTokens == null || expiredTokens.isEmpty()) {
                continue;
            }

            // 만료된 토큰 삭제
            for (String token : expiredTokens) {
                // 역인덱스와 메타데이터도 함께 삭제
                Optional<Long> userIdOpt = findUserIdByToken(token);
                Optional<String> deviceIdOpt = findDeviceIdByToken(token);

                if (userIdOpt.isPresent() && deviceIdOpt.isPresent()) {
                    deleteByUserIdAndDeviceId(userIdOpt.get(), deviceIdOpt.get());
                    totalDeleted++;
                }
            }
        }

        log.info("만료된 토큰 정리 완료 - 삭제된 토큰 수: {}", totalDeleted);

        return totalDeleted;
    }

    // 토큰 존재 여부 확인
    @Override
    public boolean existsByToken(String token) {

        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;

        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenToUserKey));
    }

    // 유틸리티 메서드
    // LocalDateTime을 Unix Timestamp(초)로 변환
    private double toTimestamp(LocalDateTime dateTime) {

        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    // TTL 계산 (만료시간까지 남은 시간 + 1일 여유)
    private long calculateTtl(LocalDateTime expiresAt) {

        long now = (long) toTimestamp(LocalDateTime.now());
        long expireTimestamp = (long) toTimestamp(expiresAt);
        long ttl = expireTimestamp - now;

        // TTL이 음수이거나 0이면 기본값(7일) 적용
        if (ttl <= 0) {
            log.warn("TTL이 음수 또는 0! 기본 TTL(7일) 적용");

            return 7 * 24 * 60 * 60;
        }

        return ttl + 86400; // +1일 여유
    }

    // DeviceInfoResponse 빌더
    private DeviceInfoResponse buildDeviceInfoResponse(
            String deviceId,
            Map<Object, Object> metadata,
            String currentDeviceId
    ) {
        return DeviceInfoResponse.builder()
                .deviceId(deviceId)
                .deviceName(metadata.get("deviceName").toString())
                .lastUsedAt(LocalDateTime.parse(metadata.get("lastUsedAt").toString()))
                .expiresAt(LocalDateTime.parse(metadata.get("expiresAt").toString()))
                .isCurrent(currentDeviceId != null && deviceId.equals(currentDeviceId))
                .ipAddress(metadata.get("ipAddress").toString())
                .userAgent(metadata.get("userAgent").toString())
                .build();
    }
}