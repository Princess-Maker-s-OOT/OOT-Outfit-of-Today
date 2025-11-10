package org.example.ootoutfitoftoday.common.client.payment;

import java.math.BigDecimal;

public interface TossPaymentsClient {

    /**
     * 토스페이먼츠 결제 승인
     * @param paymentKey 토스 결제 키
     * @param orderId 주문 ID
     * @param amount 결제 금액
     */
    void confirmPayment(String paymentKey, String orderId, BigDecimal amount);
}
