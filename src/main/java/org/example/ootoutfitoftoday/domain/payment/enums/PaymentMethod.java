package org.example.ootoutfitoftoday.domain.payment.enums;

public enum PaymentMethod {
    ACCOUNT_TRANSFER("계좌이체"),
    EASY_PAY("간편결제");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
