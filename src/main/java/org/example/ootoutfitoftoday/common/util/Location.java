package org.example.ootoutfitoftoday.common.util;

import java.math.BigDecimal;

public record Location(
        BigDecimal latitude,
        BigDecimal longitude
) {

    public static Location of(BigDecimal latitude, BigDecimal longitude) {

        return new Location(latitude, longitude);
    }
}
