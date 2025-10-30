package org.example.ootoutfitoftoday.domain.payment.enums;

public enum EasyPayProvider {
    TOSS_PAY("토스페이"),
    NAVER_PAY("네이버페이");

    private final String description;

    EasyPayProvider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}