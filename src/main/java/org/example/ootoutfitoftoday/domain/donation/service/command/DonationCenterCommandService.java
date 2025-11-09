package org.example.ootoutfitoftoday.domain.donation.service.command;

import org.example.ootoutfitoftoday.domain.donation.entity.DonationCenter;
import org.locationtech.jts.geom.Point;

public interface DonationCenterCommandService {

    // 카카오맵 API 응답 데이터로부터 기부처 생성 또는 조회
    DonationCenter createOrGet(
            String kakaoPlaceId,
            String name,
            String address,
            String phoneNumber,
            Point location,
            String description
    );
}