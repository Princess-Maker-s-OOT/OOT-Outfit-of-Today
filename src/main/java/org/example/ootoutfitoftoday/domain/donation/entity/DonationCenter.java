package org.example.ootoutfitoftoday.domain.donation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.locationtech.jts.geom.Point;


// 카카오맵 API로부터 가져온 기부처 정보를 저장
@Entity
@Table(name = "donation_centers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DonationCenter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 중복 방지를 위한 unique 제약조건
    @Column(nullable = false, unique = true, length = 50)
    private String kakaoPlaceId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = true, length = 20)
    private String phoneNumber;

    // 운영시간 (예: "월-금 09:00-18:00")
    @Column(nullable = true, length = 100)
    private String operatingHours;

    /**
     * 위치 정보 (위도/경도)
     * POINT 타입, SRID 4326 (WGS84 좌표계)
     */
    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point location;

    @Column(nullable = true, length = 255)
    private String description;

    @Builder(access = AccessLevel.PRIVATE)
    private DonationCenter(
            String kakaoPlaceId,
            String name,
            String address,
            String phoneNumber,
            String operatingHours,
            Point location,
            String description
    ) {
        this.kakaoPlaceId = kakaoPlaceId;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.operatingHours = operatingHours;
        this.location = location;
        this.description = description;
    }

    public static DonationCenter createFromKakaoMap(
            String kakaoPlaceId,
            String name,
            String address,
            String phoneNumber,
            String operatingHours,
            Point location,
            String description
    ) {
        return DonationCenter.builder()
                .kakaoPlaceId(kakaoPlaceId)
                .name(name)
                .address(address)
                .phoneNumber(phoneNumber)
                .operatingHours(operatingHours)
                .location(location)
                .description(description)
                .build();
    }
    
    // 위도 조회 편의 메서드
    public double getLatitude() {
        return location.getY();
    }

    // 경도 조회 편의 메서드
    public double getLongitude() {
        return location.getX();
    }
}