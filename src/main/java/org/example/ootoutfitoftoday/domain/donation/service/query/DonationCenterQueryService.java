package org.example.ootoutfitoftoday.domain.donation.service.query;

import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;

import java.util.List;

public interface DonationCenterQueryService {

    List<DonationCenterSearchResponse> searchNearbyDonationCenters(
            Double latitude,
            Double longitude,
            Integer radius,
            String keyword
    );
}
