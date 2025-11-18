package org.example.ootoutfitoftoday.domain.auth.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.response.DeviceInfoResponse;
import org.example.ootoutfitoftoday.domain.auth.entity.RefreshToken;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthErrorCode;
import org.example.ootoutfitoftoday.domain.auth.exception.AuthException;
import org.example.ootoutfitoftoday.domain.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthQueryServiceImpl implements AuthQueryService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public List<DeviceInfoResponse> getDeviceList(AuthUser authUser, String currentDeviceId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserIdOrderByLastUsedAtDesc(authUser.getUserId());

        boolean isValidDevice = false;
        for (RefreshToken token : tokens) {
            if (token.getDeviceId().equals(currentDeviceId)) {
                isValidDevice = true;
                break;
            }
        }

        if (!isValidDevice) {
            log.warn("디바이스 목록 조회 실패 - 유효하지 않은 디바이스 ID - userId: {}, deviceId: {}", authUser.getUserId(), currentDeviceId);
            throw new AuthException(AuthErrorCode.INVALID_DEVICE);
        }

        List<DeviceInfoResponse> deviceList = new ArrayList<>();

        for (RefreshToken token : tokens) {
            DeviceInfoResponse response = DeviceInfoResponse.builder()
                    .deviceId(token.getDeviceId())
                    .deviceName(token.getDeviceName())
                    .lastUsedAt(token.getLastUsedAt())
                    .expiresAt(token.getExpiresAt())
                    .isCurrent(token.getDeviceId().equals(currentDeviceId))
                    .ipAddress(token.getIpAddress())
                    .userAgent(token.getUserAgent())
                    .build();
            deviceList.add(response);
        }

        return deviceList;
    }
}