package org.example.ootoutfitoftoday.domain.auth.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.response.DeviceInfoResponse;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.auth.repository.RedisRefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthQueryServiceImpl implements AuthQueryService {

    //    private final RefreshTokenRepository refreshTokenRepository;
    // MySQL 리포지토리 대신 Redis 리포지토리 사용
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;


    // 유저의 모든 디바이스 목록 조회(최근 사용 순)
    @Override
    public List<DeviceInfoResponse> getDeviceList(AuthUser authUser, String currentDeviceId) {

        // 최근 사용 순으로 모든 디바이스 토큰 조회
        List<DeviceInfoResponse> devices = redisRefreshTokenRepository.findAllByUserId(authUser.getUserId());
        // currentDeviceId가 실제로 이 유저의 디바이스인지 검증
        boolean isValidDevice = false;
        for (DeviceInfoResponse device : devices) {
            if (device.getDeviceId().equals(currentDeviceId)) {
                isValidDevice = true;
                break;
            }
        }

        if (!isValidDevice) {
            log.warn("유효하지 않은 디바이스 ID - userId: {}, deviceId: {}", authUser.getUserId(), currentDeviceId);
            throw new AuthException(AuthErrorCode.INVALID_DEVICE);
        }

        // 조회 결과 리스트 생성
        List<DeviceInfoResponse> updatedDevices = new ArrayList<>();

        for (DeviceInfoResponse device : devices) {
            DeviceInfoResponse updated = DeviceInfoResponse.builder()
                    .deviceId(device.getDeviceId())
                    .deviceName(device.getDeviceName())
                    .lastUsedAt(device.getLastUsedAt())
                    .expiresAt(device.getExpiresAt())
                    .isCurrent(device.getDeviceId().equals(currentDeviceId))
                    .ipAddress(device.getIpAddress())
                    .userAgent(device.getUserAgent())
                    .build();

            updatedDevices.add(updated);
        }

        return updatedDevices;
    }
}