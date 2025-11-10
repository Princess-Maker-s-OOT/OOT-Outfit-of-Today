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

    // 유저의 모든 디바이스 목록 조회(최근 사용 순)
    @Override
    public List<DeviceInfoResponse> getDeviceList(AuthUser authUser, String currentDeviceId) {

        // 최근 사용 순으로 모든 디바이스 토큰 조회
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserIdOrderByLastUsedAtDesc(authUser.getUserId());

        // currentDeviceId가 실제로 이 유저의 디바이스인지 검증
        boolean isValidDevice = false;
        for (RefreshToken token : tokens) {
            if (token.getDeviceId().equals(currentDeviceId)) {
                isValidDevice = true;
                break;
            }
        }

        if (!isValidDevice) {
            throw new AuthException(AuthErrorCode.INVALID_DEVICE);
        }

        // 조회 결과 리스트 생성
        List<DeviceInfoResponse> deviceList = new ArrayList<>();

        // RefreshToken -> DeviceInfoResponse 변환
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

        // 변환된 리스트 반환
        return deviceList;
    }
}
