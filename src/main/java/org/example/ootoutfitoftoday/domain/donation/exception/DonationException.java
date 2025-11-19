package org.example.ootoutfitoftoday.domain.donation.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class DonationException extends GlobalException {

    public DonationException(DonationErrorCode donationErrorCode) {
        super(donationErrorCode);
    }
}
