package org.example.ootoutfitoftoday.domain.user.dto.request;

import java.math.BigDecimal;

public record UserUpdateTradeLocationRequest(
        String tradeAddress,
        BigDecimal tradeLatitude,
        BigDecimal tradeLongitude
) {
}