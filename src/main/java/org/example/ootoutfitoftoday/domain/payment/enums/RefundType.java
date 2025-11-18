package org.example.ootoutfitoftoday.domain.payment.enums;

public enum RefundType {
    SELLER_CANCELLED("판매자 취소"),
    BUYER_CANCELLED("구매자 취소"),
    MUTUAL_AGREEMENT("상호 합의"),
    PRODUCT_UNAVAILABLE("상품 준비 불가"),
    TIMEOUT("거래 시간 초과"),
    ETC("기타");

    private final String description;

    RefundType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}