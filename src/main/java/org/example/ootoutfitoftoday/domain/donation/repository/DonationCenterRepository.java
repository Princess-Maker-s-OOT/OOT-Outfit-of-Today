package org.example.ootoutfitoftoday.domain.donation.repository;

import org.example.ootoutfitoftoday.domain.donation.entity.DonationCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DonationCenterRepository extends JpaRepository<DonationCenter, Long> {

    Optional<DonationCenter> findByKakaoPlaceId(String kakaoPlaceId);
}