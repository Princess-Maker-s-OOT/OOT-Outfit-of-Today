package org.example.ootoutfitoftoday.domain.payment.enums;

public enum PaymentStatus {
    ESCROWED("예치중"),  // 안심결제 핵심
    SETTLED("정산완료"),  // 판매자 전달
    REFUNDED("환불완료");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
