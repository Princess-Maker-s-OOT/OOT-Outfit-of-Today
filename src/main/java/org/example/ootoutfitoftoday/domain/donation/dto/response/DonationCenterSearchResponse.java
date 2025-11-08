package org.example.ootoutfitoftoday.domain.donation.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.donation.entity.DonationCenter;

/**
 * 기부처 검색 응답 DTO
 */
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
        Integer distance  // 사용자 위치로부터의 거리 (미터 단위)
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
                .distance(null)  // 기본적으로는 거리 정보 없음
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