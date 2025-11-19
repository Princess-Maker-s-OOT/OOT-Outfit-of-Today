package org.example.ootoutfitoftoday.toss.client;

import org.example.ootoutfitoftoday.toss.dto.TossConfirmResult;

import java.math.BigDecimal;

public interface TossPaymentsClient {

    TossConfirmResult confirmPayment(String paymentKey, String orderId, BigDecimal amount);
}
