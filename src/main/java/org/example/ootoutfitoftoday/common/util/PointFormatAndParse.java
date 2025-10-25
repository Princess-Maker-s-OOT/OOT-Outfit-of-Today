package org.example.ootoutfitoftoday.common.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class PointFormatAndParse {

    public static String format(BigDecimal tradeLatitude, BigDecimal tradeLongitude) {

        return String.format("POINT(%s %s)", tradeLatitude, tradeLongitude);
    }

    public static Location parse(String tradeLocation) {
        String coordinateLocation = tradeLocation.substring(6, tradeLocation.length() - 1).trim();

        String[] location = coordinateLocation.split(" ");

        String longitudeStr = location[0];
        String latitudeStr = location[1];

        BigDecimal tradeLongitude = new BigDecimal(longitudeStr);
        BigDecimal tradeLatitude = new BigDecimal(latitudeStr);

        return Location.from(tradeLatitude, tradeLongitude);
    }
}
