package org.example.ootoutfitoftoday.toss.dto;

import java.time.LocalDateTime;

public record TossConfirmResult(
        String receiptUrl,
        LocalDateTime approvedAt
) {
}