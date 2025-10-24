package org.example.ootoutfitoftoday.common.util;

import java.math.BigDecimal;

public class PointFormater {
    
    public static String format(BigDecimal tradeLatitude, BigDecimal tradeLongitude) {
        return String.format("POINT(%s %s)", tradeLatitude, tradeLongitude);
    }
}
