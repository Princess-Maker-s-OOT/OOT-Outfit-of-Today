package org.example.ootoutfitoftoday.Toss.dto;

import java.time.LocalDateTime;

public record TossConfirmResult(
        String receiptUrl,
        LocalDateTime approvedAt
) {}
