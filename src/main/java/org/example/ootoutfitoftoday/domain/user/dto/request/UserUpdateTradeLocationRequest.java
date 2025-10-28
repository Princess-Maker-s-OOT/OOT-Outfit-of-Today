package org.example.ootoutfitoftoday.domain.user.dto.request;

import java.math.BigDecimal;

public record UserUpdateTradeLocationRequest(
        String tradeAddress,            // 주소
        BigDecimal tradeLatitude,       // 위도
        BigDecimal tradeLongitude       // 경도
) {
}
