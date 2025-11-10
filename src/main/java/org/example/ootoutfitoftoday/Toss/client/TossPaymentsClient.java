package org.example.ootoutfitoftoday.Toss.client;

import org.example.ootoutfitoftoday.Toss.dto.TossConfirmResult;

import java.math.BigDecimal;

public interface TossPaymentsClient {

    TossConfirmResult confirmPayment(String paymentKey, String orderId, BigDecimal amount);
}
