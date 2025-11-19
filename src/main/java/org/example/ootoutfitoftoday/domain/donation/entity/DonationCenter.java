package org.example.ootoutfitoftoday.domain.donation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "donation_centers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DonationCenter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String kakaoPlaceId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = true, length = 20)
    private String phoneNumber;

    @Column(nullable = true, length = 100)
    private String operatingHours;

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

    public double getLatitude() {

        return location.getY();
    }

    public double getLongitude() {

        return location.getX();
    }
}