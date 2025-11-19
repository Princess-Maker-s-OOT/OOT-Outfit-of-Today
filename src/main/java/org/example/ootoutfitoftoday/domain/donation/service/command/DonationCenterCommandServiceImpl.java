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
@Transactional
public class DonationCenterCommandServiceImpl implements DonationCenterCommandService {

    private final DonationCenterRepository donationCenterRepository;

    @Override
    public DonationCenter createOrGet(
            String kakaoPlaceId,
            String name,
            String address,
            String phoneNumber,
            Point location,
            String description
    ) {
        log.debug("createOrGet 호출 - 카카오ID: {}, 이름: {}", kakaoPlaceId, name);
        long startTime = System.currentTimeMillis();

        return donationCenterRepository
                .findByKakaoPlaceId(kakaoPlaceId)
                .map(existingCenter -> {
                    long queryTime = System.currentTimeMillis() - startTime;
                    log.debug("기존 기부처 조회 완료 - 카카오ID: {}, 기부처ID: {}, 이름: {}, 조회시간: {}ms",
                            kakaoPlaceId, existingCenter.getId(), existingCenter.getName(), queryTime);

                    return existingCenter;
                })
                .orElseGet(() -> {
                    long queryTime = System.currentTimeMillis() - startTime;
                    log.debug("기존 기부처 없음 - 카카오ID: {}, 조회시간: {}ms", kakaoPlaceId, queryTime);

                    log.info("신규 기부처 생성 시작 - 카카오ID: {}, 이름: {}, 주소: {}, 전화번호: {}",
                            kakaoPlaceId, name, address, phoneNumber != null ? phoneNumber : "없음");

                    DonationCenter newCenter = DonationCenter.createFromKakaoMap(
                            kakaoPlaceId,
                            name,
                            address,
                            phoneNumber,
                            null,
                            location,
                            description
                    );
                    log.debug("기부처 엔티티 생성 완료 - 카카오ID: {}", kakaoPlaceId);

                    long saveStartTime = System.currentTimeMillis();
                    DonationCenter savedCenter = donationCenterRepository.save(newCenter);
                    long saveTime = System.currentTimeMillis() - saveStartTime;

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("신규 기부처 저장 완료 - 기부처ID: {}, 카카오ID: {}, 이름: {}, 저장시간: {}ms, 전체시간: {}ms",
                            savedCenter.getId(), kakaoPlaceId, name, saveTime, totalTime);

                    return savedCenter;
                });
    }
}