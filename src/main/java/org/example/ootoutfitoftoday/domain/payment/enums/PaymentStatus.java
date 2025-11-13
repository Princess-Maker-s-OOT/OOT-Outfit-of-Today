package org.example.ootoutfitoftoday.domain.payment.enums;

public enum PaymentStatus {
    PENDING("결제대기중"),
    FAILED("결제 실패"),
    ESCROWED("예치중"),
    SETTLED("정산완료"),
    REFUNDED("환불완료");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
