package org.example.ootoutfitoftoday.Toss.client;

import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.Toss.dto.TossConfirmResult;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Primary
@Profile("local")
@Component
public class MockTossPaymentsClient implements TossPaymentsClient {

    @Override
    public TossConfirmResult confirmPayment(
            String paymentKey,
            String orderId,
            BigDecimal amount
    ) {
        log.info("[Mock] 토스 결제 승인 호출");
        log.info("- paymentKey: {}", paymentKey);
        log.info("- orderId: {}", orderId);
        log.info("- amount: {}", amount);

        String mockReceiptUrl = "https://mock-receipt.tosspayments.com/receipts/" + orderId;
        LocalDateTime mockApprovedAt = LocalDateTime.now();

        log.info("[Mock] 결제 승인 성공 (가짜 응답)");

        return new TossConfirmResult(mockReceiptUrl, mockApprovedAt);
    }
}
