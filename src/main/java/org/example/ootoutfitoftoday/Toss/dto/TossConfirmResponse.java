package org.example.ootoutfitoftoday.Toss.dto;

public record TossConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        Integer totalAmount,
        String approvedAt,
        String receiptUrl
) {}
