package org.example.ootoutfitoftoday.domain.donation.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.donation.entity.DonationCenter;

/**
 * 기부처 응답 DTO
 */
@Builder(access = AccessLevel.PRIVATE)
public record DonationCenterResponse(
        
        Long donationCenterId,
        String kakaoPlaceId,
        String name,
        String address,
        String phoneNumber,
        String operatingHours,
        Double latitude,
        Double longitude,
        String description
) {
    public static DonationCenterResponse from(DonationCenter center) {

        return DonationCenterResponse.builder()
                .donationCenterId(center.getId())
                .kakaoPlaceId(center.getKakaoPlaceId())
                .name(center.getName())
                .address(center.getAddress())
                .phoneNumber(center.getPhoneNumber())
                .operatingHours(center.getOperatingHours())
                .latitude(center.getLatitude())
                .longitude(center.getLongitude())
                .description(center.getDescription())
                .build();
    }
}