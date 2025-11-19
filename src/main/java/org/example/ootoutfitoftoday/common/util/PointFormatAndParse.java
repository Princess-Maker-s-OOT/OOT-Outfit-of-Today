package org.example.ootoutfitoftoday.common.util;

import java.math.BigDecimal;

public class PointFormatAndParse {

    public static String format(BigDecimal tradeLatitude, BigDecimal tradeLongitude) {

        return String.format("POINT(%s %s)", tradeLatitude, tradeLongitude);
    }

    public static Location parse(String tradeLocation) {
        String coordinateLocation = tradeLocation.substring(6, tradeLocation.length() - 1).trim();

        String[] location = coordinateLocation.split(" ");

        String latitudeStr = location[0];
        String longitudeStr = location[1];

        BigDecimal tradeLatitude = new BigDecimal(latitudeStr);
        BigDecimal tradeLongitude = new BigDecimal(longitudeStr);

        return Location.of(tradeLatitude, tradeLongitude);
    }
}
