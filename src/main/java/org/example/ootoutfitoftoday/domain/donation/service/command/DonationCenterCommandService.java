package org.example.ootoutfitoftoday.domain.donation.service.command;

import org.example.ootoutfitoftoday.domain.donation.entity.DonationCenter;
import org.locationtech.jts.geom.Point;

public interface DonationCenterCommandService {

    DonationCenter createOrGet(
            String kakaoPlaceId,
            String name,
            String address,
            String phoneNumber,
            Point location,
            String description
    );
}