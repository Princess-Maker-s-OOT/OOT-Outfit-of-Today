package org.example.ootoutfitoftoday.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.dto.response.DeviceInfoResponse;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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
 * 최적화:
 * - KEYS(모든 키 한번에 스캔-완전 블로킹) 대신 SCAN 사용(논블로킹)
 * - N+1 문제 해결을 위한 Pipeline 사용:
 * 기존: 여러 번의 개별 Redis 호출(네트워크 왕복 여러 번)
 * 최적화: Pipeline으로 모든 Redis 명령어를 모아서 한 번에 전송(네트워크 왕복 1번)
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
        String userTokensKey = USER_TOKENS_PREFIX + userId;    // refresh_tokens:user:123
        double score = toTimestamp(expiresAt);                 // 만료시간을 숫자로 변환 (정렬용)
        long ttlSeconds = calculateTtl(expiresAt);             // 데이터 자동 삭제 시간 계산

        // Pipeline으로 모든 저장 작업 한번에 수행
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            // 1. Sorted Set에 토큰 저장
            // - Key: "refresh_tokens:user:123"
            // - Member: 토큰 값
            // - Score: 만료시간(이 값으로 정렬됨)
            stringRedisConn.zAdd(userTokensKey, score, token);
            stringRedisConn.expire(userTokensKey, ttlSeconds);    // TTL 설정(자동 삭제)

            // 2. 역인덱스 저장(토큰 -> 사용자 정보)
            // - 목적: 토큰만 가지고 userId, deviceId를 빠르게 찾기 위함
            // - Key: "token_to_user:abc123xyz"
            String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
            stringRedisConn.hSet(tokenToUserKey, "userId", userId.toString());
            stringRedisConn.hSet(tokenToUserKey, "deviceId", deviceId);
            stringRedisConn.expire(tokenToUserKey, ttlSeconds);

            // 3. 디바이스 메타데이터 저장
            // - Key: "device_meta:123:deviceABC"
            // - MySQL의 RefreshToken 테이블과 동일한 정보 저장
            String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
            stringRedisConn.hSet(deviceMetaKey, "userId", userId.toString());
            stringRedisConn.hSet(deviceMetaKey, "deviceId", deviceId);
            stringRedisConn.hSet(deviceMetaKey, "deviceName", deviceName != null ? deviceName : "Unknown Device");
            stringRedisConn.hSet(deviceMetaKey, "token", token);
            stringRedisConn.hSet(deviceMetaKey, "expiresAt", expiresAt.toString());
            stringRedisConn.hSet(deviceMetaKey, "lastUsedAt", LocalDateTime.now().toString());
            stringRedisConn.hSet(deviceMetaKey, "ipAddress", ipAddress != null ? ipAddress : "");
            stringRedisConn.hSet(deviceMetaKey, "userAgent", userAgent != null ? userAgent : "");
            stringRedisConn.expire(deviceMetaKey, ttlSeconds);

            return null;    // Pipeline은 반환값 필요 없음
        });

        log.debug("Redis에 토큰 저장 완료 - userId: {}, deviceId: {}", userId, deviceId);
    }

    /**
     * 토큰으로 사용자 ID 조회(역인덱스 활용)
     * 역인덱스
     * - 일반적인 검색: userId로 토큰을 찾음
     * - 역인덱스: 토큰으로 userId를 찾음(반대 방향)
     * - 예시) 책의 색인(index) - 단어로 페이지 번호 찾기
     */
    @Override
    public Optional<Long> findUserIdByToken(String token) {

        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;    // "token_to_user:abc123"
        // Hash에서 "userId" 필드 값 가져오기
        Object userIdObj = redisTemplate.opsForHash().get(tokenToUserKey, "userId");

        if (userIdObj == null) {

            return Optional.empty();
        }

        return Optional.of(Long.parseLong(userIdObj.toString()));
    }

    /**
     * 토큰으로 디바이스 ID 조회(역인덱스 활용)
     */
    @Override
    public Optional<String> findDeviceIdByToken(String token) {

        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
        Object deviceIdObj = redisTemplate.opsForHash().get(tokenToUserKey, "deviceId");

        return Optional.ofNullable(deviceIdObj != null ? deviceIdObj.toString() : null);
    }

    /**
     * 사용자의 특정 디바이스 토큰 조회
     * 사용 예시: 같은 디바이스에서 다시 로그인 시 기존 토큰 찾기
     */
    @Override
    public Optional<String> findByUserIdAndDeviceId(Long userId, String deviceId) {

        String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
        // 디바이스 메타데이터에서 토큰 값 가져오기
        Object tokenObj = redisTemplate.opsForHash().get(deviceMetaKey, "token");

        return Optional.ofNullable(tokenObj != null ? tokenObj.toString() : null);
    }

    /**
     * 토큰의 메타데이터 조회 (디바이스 정보 포함)
     * Pipeline 최적화:
     * - 기존: 역인덱스 조회(1회) + 메타데이터 조회(1회) = 2회
     * - 최적화: Pipeline으로 한 번에 조회 = 1회
     */
    @Override
    public Optional<DeviceInfoResponse> findTokenMetadata(String token) {

        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;

        // Pipeline으로 역인덱스와 메타데이터 한번에 조회
        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
            stringRedisConn.hGetAll(tokenToUserKey);    // Hash의 모든 필드 가져오기

            return null;
        });

        // Pipeline 결과는 List로 반환됨(순서대로)
        @SuppressWarnings("unchecked")
        Map<Object, Object> tokenInfo = (Map<Object, Object>) results.get(0);

        if (tokenInfo == null || tokenInfo.isEmpty()) {

            return Optional.empty();
        }

        // userId와 deviceId 추출
        Long userId = Long.parseLong(tokenInfo.get("userId").toString());
        String deviceId = tokenInfo.get("deviceId").toString();

        // 메타데이터(디바이스 상세 정보) 조회
        String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
        Map<Object, Object> metadata = redisTemplate.opsForHash().entries(deviceMetaKey);

        if (metadata.isEmpty()) {

            return Optional.empty();
        }

        return Optional.of(buildDeviceInfoResponse(deviceId, metadata, null));
    }

    /**
     * 사용자의 모든 디바이스 목록 조회 (최근 사용순 정렬)
     * N+1 문제 해결:
     * - 1번 조회 후 결과 개수(N)만큼 추가 조회하는 문제
     * - 예시) 디바이스 10개 → 1 + 5 + 5 = 11번 Redis 호출
     * Pipeline 최적화:
     * - 기존: 토큰 조회(1회) + deviceId 조회(N회) + 메타데이터 조회(N회) = 1+N+N회
     * - 최적화: 토큰 조회(1회) + Pipeline deviceId(1회) + Pipeline 메타데이터(1회) = 3회
     * - 성능: 디바이스가 10개면 11회 → 3회(3.6배 개선!)
     */
    @Override
    public List<DeviceInfoResponse> findAllByUserId(Long userId) {

        String userTokensKey = USER_TOKENS_PREFIX + userId;

        // 1. Sorted Set에서 모든 토큰 조회(score 기준 오름차순)
        Set<String> tokens = redisTemplate.opsForZSet().range(userTokensKey, 0, -1);

        if (tokens == null || tokens.isEmpty()) {

            return Collections.emptyList();
        }

        List<String> tokenList = new ArrayList<>(tokens);

        // 2. Pipeline으로 역인덱스 일괄 조회
        // - 기존 방식: for문 안에서 토큰마다 Redis 호출(N번)
        // - Pipeline: 모든 명령을 모아서 한 번에 전송(1번)
        // for문이 Pipeline 안에 있음 -> 명령만 모음, 1번만 전송
        List<Object> deviceIdResults = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
            for (String token : tokenList) {
                // 여기서는 Redis와 통신하지 않음!
                // 명령어만 큐에 추가(즉시 실행 X)
                String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
                stringRedisConn.hGet(tokenToUserKey, "deviceId");
            }

            return null;    // 이 시점에 모든 명령을 한 번에 전송
        });

        // 3. deviceId 추출
        List<String> deviceIds = new ArrayList<>();
        for (Object result : deviceIdResults) {
            if (result != null) {
                deviceIds.add(result.toString());
            }
        }

        if (deviceIds.isEmpty()) {

            return Collections.emptyList();
        }

        // 4. Pipeline으로 모든 디바이스의 메타데이터 일괄 조회
        List<Object> metadataResults = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
            for (String deviceId : deviceIds) {
                String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
                stringRedisConn.hGetAll(deviceMetaKey);
            }

            return null;
        });

        // 5. DeviceInfoResponse 객체(응답) 생성
        List<DeviceInfoResponse> devices = new ArrayList<>();
        for (int i = 0; i < deviceIds.size(); i++) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> metadata = (Map<Object, Object>) metadataResults.get(i);

            if (metadata != null && !metadata.isEmpty()) {
                devices.add(buildDeviceInfoResponse(deviceIds.get(i), metadata, null));
            }
        }

        // lastUsedAt 기준 내림차순 정렬(최근 사용 순)
        devices.sort((d1, d2) -> d2.getLastUsedAt().compareTo(d1.getLastUsedAt()));

        return devices;
    }

    /**
     * 사용자의 디바이스 개수 조회
     * Sorted Set의 zCard:
     * - Set의 원소 개수를 O(1) 시간에 반환
     * - 매우 빠른 연산
     */
    @Override
    public long countByUserId(Long userId) {

        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Long count = redisTemplate.opsForZSet().zCard(userTokensKey);

        return count != null ? count : 0L;
    }

    /**
     * 가장 오래된 디바이스 조회(lastUsedAt 기준)
     */
    @Override
    public Optional<DeviceInfoResponse> findOldestDeviceByUserId(Long userId) {
        // findAllByUserId가 이미 Pipeline으로 최적화되어 있으므로 재사용
        List<DeviceInfoResponse> devices = findAllByUserId(userId);

        if (devices.isEmpty()) {

            return Optional.empty();
        }

        // lastUsedAt 기준 가장 오래된 디바이스 찾기
        DeviceInfoResponse oldestDevice = devices.get(0);    // 첫 번째를 기준으로

        for (DeviceInfoResponse device : devices) {
            // lastUsedAt이 더 오래된(이전) 것을 찾으면 교체
            if (device.getLastUsedAt().isBefore(oldestDevice.getLastUsedAt())) {
                oldestDevice = device;
            }
        }

        return Optional.of(oldestDevice);
    }

    /**
     * 토큰 업데이트 (RTR - Refresh Token Rotation)
     * RTR
     * - 리프레시 토큰 사용 시마다 새로운 토큰으로 교체하는 보안 기법
     * - 토큰 탈취 시 피해 최소화
     * 동작 방식:
     * 1. 기존 토큰 삭제
     * 2. 새 토큰 저장 (동일한 deviceId로)
     * Pipeline 최적화:
     * - 기존: 삭제(여러 번) + 저장(여러 번)
     * - 최적화: Pipeline 삭제(1번) + Pipeline 저장(1번)
     */

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
        // 기존 디바이스 이름 유지(기존 메타데이터에서 deviceName 가져오기)
        String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
        Object deviceNameObj = redisTemplate.opsForHash().get(deviceMetaKey, "deviceName");
        String deviceName = deviceNameObj != null ? deviceNameObj.toString() : "Unknown Device";

        String userTokensKey = USER_TOKENS_PREFIX + userId;
        String oldTokenToUserKey = TOKEN_TO_USER_PREFIX + oldToken;

        // Pipeline으로 삭제와 저장을 함께 수행
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            // Sorted Set에서 기존 토큰 제거
            stringRedisConn.zRem(userTokensKey, oldToken);
            // 역인덱스 삭제
            stringRedisConn.del(oldTokenToUserKey);

            return null;
        });

        // 새 토큰 저장
        save(userId, deviceId, deviceName, newToken, newExpiresAt, ipAddress, userAgent);

        log.debug("토큰 업데이트 완료 - userId: {}, deviceId: {}", userId, deviceId);
    }

    /**
     * 특정 디바이스 삭제(로그아웃)
     * 삭제할 데이터:
     * 1. Sorted Set의 토큰(refresh_tokens:user:123)
     * 2. 역인덱스(token_to_user:abc123)
     * 3. 디바이스 메타데이터(device_meta:123:deviceABC)
     * Pipeline 최적화:
     * - 기존: 3개 키를 각각 삭제(3번의 네트워크 왕복)
     * - 최적화: Pipeline으로 한 번에 삭제(1번의 네트워크 왕복)
     */
    @Override
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        // 토큰 조회(삭제할 데이터 찾기)
        Optional<String> tokenOpt = findByUserIdAndDeviceId(userId, deviceId);

        if (tokenOpt.isEmpty()) {
            log.warn("삭제할 토큰을 찾을 수 없음 - userId: {}, deviceId: {}", userId, deviceId);

            return;
        }

        String token = tokenOpt.get();
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
        String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;

        // Pipeline으로 모든 삭제 작업 한번에 수행
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            // 1. Sorted Set에서 토큰 제거
            stringRedisConn.zRem(userTokensKey, token);
            // 2. 역인덱스 삭제
            stringRedisConn.del(tokenToUserKey);
            // 3. 디바이스 메타데이터 삭제
            stringRedisConn.del(deviceMetaKey);

            return null;
        });

        log.info("디바이스 삭제 완료 - userId: {}, deviceId: {}", userId, deviceId);
    }

    /**
     * 사용자의 모든 디바이스 삭제(전체 로그아웃)
     * 사용 시나리오:
     * - 회원 탈퇴 시
     * - 보안 위협 감지 시 모든 세션 종료
     * - "모든 기기에서 로그아웃" 기능
     * Pipeline 최적화:
     * - 기존: 토큰 개수(N) × 3개 키 = N×3번 호출
     * - 최적화: 토큰 조회(1번) + Pipeline deviceId 조회(1번) + Pipeline 삭제(1번) = 3번
     */
    @Override
    public void deleteAllByUserId(Long userId) {

        String userTokensKey = USER_TOKENS_PREFIX + userId;

        // 1. 모든 토큰 조회
        Set<String> tokens = redisTemplate.opsForZSet().range(userTokensKey, 0, -1);

        if (tokens == null || tokens.isEmpty()) {

            return;
        }

        List<String> tokenList = new ArrayList<>(tokens);

        // 2. Pipeline으로 deviceId 일괄 조회
        List<Object> deviceIdResults = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
            for (String token : tokenList) {
                String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
                stringRedisConn.hGet(tokenToUserKey, "deviceId");
            }

            return null;
        });

        // 3. Pipeline으로 모든 관련 데이터 삭제
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            // 모든 역인덱스 삭제
            for (String token : tokenList) {
                String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
                stringRedisConn.del(tokenToUserKey);
            }

            // 모든 메타데이터 삭제
            for (int i = 0; i < deviceIdResults.size(); i++) {
                if (deviceIdResults.get(i) != null) {
                    String deviceId = deviceIdResults.get(i).toString();
                    String deviceMetaKey = DEVICE_META_PREFIX + userId + ":" + deviceId;
                    stringRedisConn.del(deviceMetaKey);
                }
            }

            // Sorted Set 전체 삭제(사용자의 모든 토큰)
            stringRedisConn.del(userTokensKey);

            return null;
        });

        log.info("사용자의 모든 디바이스 삭제 완료 - userId: {}, 삭제된 토큰 수: {}", userId, tokens.size());
    }

    /**
     * 만료된 토큰 정리(스케줄러에서 주기적으로 호출)
     * Sorted Set의 score(만료시간)를 기준으로 현재 시간 이전 토큰 삭제
     * KEYS vs SCAN의 중요한 차이:
     * KEYS 명령어:
     * - 전체 키스페이스를 한 번에 스캔
     * - Redis 서버 완전 블로킹 (모든 요청 멈춤)
     * - 키가 많으면 수 초간 서비스 중단 발생
     * - 예: 키 100만개 → 2~3초 블로킹 → 서비스 장애
     * SCAN 명령어(권장):
     * - 커서 기반으로 조금씩 스캔
     * - 논블로킹 (다른 요청 정상 처리 가능)
     * - 메모리에 부담 없음
     * - 예: 키 100만개 → 조금씩 처리 → 서비스 정상 운영
     * Pipeline 최적화 추가:
     * - SCAN으로 키 찾기(논블로킹)
     * - Pipeline으로 삭제 작업 일괄 처리(빠른 삭제)
     */
    @Override
    public long deleteExpiredTokens() {

        long totalDeleted = 0;
        double currentTimestamp = toTimestamp(LocalDateTime.now());

        // SCAN으로 논블로킹 방식으로 키 조회\
        // "refresh_tokens:user:*" 패턴의 키를 찾음
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(USER_TOKENS_PREFIX + "*")    // 패턴 매칭
                .count(100)                                // 한번에 스캔할 키 개수
                .build();

        // try-with-resources: cursor 자동 close
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            // cursor.hasNext()로 조금씩 스캔(논블로킹)
            while (cursor.hasNext()) {
                String userTokensKey = cursor.next();    // 다음 키 가져오기

                // 해당 사용자의 만료된 토큰 조회
                // 현재 시간보다 이전 score를 가진 토큰 조회
                // rangeByScore: score가 0~현재시간 사이인 토큰 = 만료된 토큰
                Set<String> expiredTokens = redisTemplate.opsForZSet()
                        .rangeByScore(userTokensKey, 0, currentTimestamp);

                if (expiredTokens == null || expiredTokens.isEmpty()) {
                    continue;    // 만료된 토큰 없음, 다음 사용자로
                }

                List<String> expiredTokenList = new ArrayList<>(expiredTokens);

                // Pipeline으로 userId, deviceId 일괄 조회
                List<Object> userInfoResults = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
                    for (String token : expiredTokenList) {
                        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;
                        stringRedisConn.hGetAll(tokenToUserKey);    // 역인덱스 정보 가져오기
                    }

                    return null;
                });

                // 삭제할 토큰 정보 수집
                List<TokenDeleteInfo> deleteInfos = new ArrayList<>();
                for (int i = 0; i < userInfoResults.size(); i++) {
                    @SuppressWarnings("unchecked")
                    Map<Object, Object> userInfo = (Map<Object, Object>) userInfoResults.get(i);

                    if (userInfo != null && !userInfo.isEmpty()) {
                        Long userId = Long.parseLong(userInfo.get("userId").toString());
                        String deviceId = userInfo.get("deviceId").toString();
                        deleteInfos.add(new TokenDeleteInfo(userId, deviceId, expiredTokenList.get(i)));
                    }
                }

                // Pipeline으로 모든 만료 토큰 일괄 삭제
                if (!deleteInfos.isEmpty()) {
                    redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                        StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

                        for (TokenDeleteInfo info : deleteInfos) {
                            String tokenKey = USER_TOKENS_PREFIX + info.userId;
                            String tokenToUserKey = TOKEN_TO_USER_PREFIX + info.token;
                            String deviceMetaKey = DEVICE_META_PREFIX + info.userId + ":" + info.deviceId;

                            // 3개 데이터 구조에서 모두 삭제
                            stringRedisConn.zRem(tokenKey, info.token);    // Sorted Set
                            stringRedisConn.del(tokenToUserKey);           // 역인덱스
                            stringRedisConn.del(deviceMetaKey);            // 메타데이터
                        }

                        return null;
                    });

                    totalDeleted += deleteInfos.size();
                }
            }
        }

        log.info("만료된 토큰 정리 완료 - 삭제된 토큰 수: {}", totalDeleted);

        return totalDeleted;
    }

    /**
     * 토큰 존재 여부 확인
     * 사용 목적:
     * - 토큰 유효성 빠른 체크
     * - 역인덱스 키 존재 여부로 판단
     */
    @Override
    public boolean existsByToken(String token) {

        String tokenToUserKey = TOKEN_TO_USER_PREFIX + token;

        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenToUserKey));
    }

    // 유틸리티 메서드

    /**
     * LocalDateTime을 Unix Timestamp(초)로 변환
     * 필요성
     * - Redis Sorted Set의 score는 숫자(double)만 가능
     * - 시간을 숫자로 변환해서 저장하면 시간순 정렬 가능
     * 예시:
     * - 2024-01-01 12:00:00 → 1704096000.0
     * - 2024-01-01 13:00:00 → 1704099600.0
     * - score가 작을수록 오래된 것
     */
    private double toTimestamp(LocalDateTime dateTime) {

        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    /**
     * TTL(Time To Live) 계산
     * TTL:
     * - Redis 키가 자동으로 삭제되는 시간
     * - 만료된 데이터를 자동으로 정리해줌
     * 계산 방식:
     * - (토큰 만료시간 - 현재시간) + 1일 여유
     * - 여유를 두는 이유: 로그 분석, 동시성 문제 방지 등
     */
    private long calculateTtl(LocalDateTime expiresAt) {

        long now = (long) toTimestamp(LocalDateTime.now());
        long expireTimestamp = (long) toTimestamp(expiresAt);
        long ttl = expireTimestamp - now;

        // TTL이 음수이거나 0이면 기본값(7일) 적용
        if (ttl <= 0) {
            log.warn("TTL이 음수 또는 0! 기본 TTL(7일) 적용");

            return 7 * 24 * 60 * 60;
        }

        return ttl + 86400;    // +1일 여유
    }

    /**
     * Redis 데이터를 DeviceInfoResponse 객체로 변환
     */
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

    /**
     * 토큰 삭제 정보를 담는 내부 헬퍼 클래스
     * Pipeline에서 여러 토큰을 한 번에 삭제할 때 사용
     * - userId: 사용자 ID
     * - deviceId: 디바이스 ID
     * - token: 토큰 값
     */
    private static class TokenDeleteInfo {

        final Long userId;
        final String deviceId;
        final String token;

        TokenDeleteInfo(Long userId, String deviceId, String token) {

            this.userId = userId;
            this.deviceId = deviceId;
            this.token = token;
        }
    }
}