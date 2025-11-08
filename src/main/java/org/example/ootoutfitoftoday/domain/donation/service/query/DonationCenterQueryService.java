package org.example.ootoutfitoftoday.domain.donation.service.query;

import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;

import java.util.List;

/**
 * 기부처 조회 서비스
 */
public interface DonationCenterQueryService {

    // 주변 기부처 검색
    List<DonationCenterSearchResponse> searchNearbyDonationCenters(
            Double latitude,
            Double longitude,
            Integer radius,
            String keyword
    );
}
