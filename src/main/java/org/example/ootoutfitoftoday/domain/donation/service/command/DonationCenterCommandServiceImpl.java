package org.example.ootoutfitoftoday.domain.donation.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.donation.entity.DonationCenter;
import org.example.ootoutfitoftoday.domain.donation.repository.DonationCenterRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationCenterCommandServiceImpl implements DonationCenterCommandService {

    private final DonationCenterRepository donationCenterRepository;

    /**
     * 카카오맵 API 응답 데이터로부터 기부처 생성 또는 조회
     * 이미 존재하는 경우 기존 데이터 반환, 없으면 새로 생성하여 저장
     */
    @Override
    @Transactional
    public DonationCenter createOrGet(
            String kakaoPlaceId,
            String name,
            String address,
            String phoneNumber,
            Point location,
            String description
    ) {

        return donationCenterRepository
                .findByKakaoPlaceId(kakaoPlaceId)
                .orElseGet(() -> {
                    log.debug("새로운 기부처 생성: kakaoPlaceId={}, name={}", kakaoPlaceId, name);

                    DonationCenter newCenter = DonationCenter.createFromKakaoMap(
                            kakaoPlaceId,
                            name,
                            address,
                            phoneNumber,
                            null,  // 운영시간 정보는 카카오맵 기본 API에서 제공하지 않음
                            location,
                            description
                    );

                    return donationCenterRepository.save(newCenter);
                });
    }
}