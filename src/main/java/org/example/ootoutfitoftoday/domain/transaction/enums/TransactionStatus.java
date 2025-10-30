package org.example.ootoutfitoftoday.domain.transaction.enums;

public enum TransactionStatus {
    PENDING_APPROVAL("판매자 수락 대기"),
    APPROVED("판매자 수락 완료"),  // 거래중
    CONFIRMED("구매확정"),
    CANCELLED_BY_BUYER("구매자 취소"),  // 판매자 수락 전만 가능
    CANCELLED_BY_SELLER("판매자 취소");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
