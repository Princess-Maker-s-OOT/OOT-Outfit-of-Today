package org.example.ootoutfitoftoday.toss.dto;

public record TossConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        Integer totalAmount,
        String approvedAt,
        String receiptUrl
) {
}