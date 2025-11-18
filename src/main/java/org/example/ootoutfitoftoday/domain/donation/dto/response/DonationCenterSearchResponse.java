package org.example.ootoutfitoftoday.domain.donation.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.donation.entity.DonationCenter;

@Builder(access = AccessLevel.PRIVATE)
public record DonationCenterSearchResponse(
        Long donationCenterId,
        String kakaoPlaceId,
        String name,
        String address,
        String phoneNumber,
        String operatingHours,
        Double latitude,
        Double longitude,
        String description,
        Integer distance
) {
    public static DonationCenterSearchResponse from(DonationCenter center) {

        return DonationCenterSearchResponse.builder()
                .donationCenterId(center.getId())
                .kakaoPlaceId(center.getKakaoPlaceId())
                .name(center.getName())
                .address(center.getAddress())
                .phoneNumber(center.getPhoneNumber())
                .operatingHours(center.getOperatingHours())
                .latitude(center.getLatitude())
                .longitude(center.getLongitude())
                .description(center.getDescription())
                .distance(null)
                .build();
    }

    public static DonationCenterSearchResponse fromWithDistance(DonationCenter center, Integer distance) {

        return DonationCenterSearchResponse.builder()
                .donationCenterId(center.getId())
                .kakaoPlaceId(center.getKakaoPlaceId())
                .name(center.getName())
                .address(center.getAddress())
                .phoneNumber(center.getPhoneNumber())
                .operatingHours(center.getOperatingHours())
                .latitude(center.getLatitude())
                .longitude(center.getLongitude())
                .description(center.getDescription())
                .distance(distance)
                .build();
    }
}