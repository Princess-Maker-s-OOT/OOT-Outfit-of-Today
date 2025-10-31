package org.example.ootoutfitoftoday.domain.payment.enums;

public enum RefundType {
    SELLER_CANCELLED("판매자 취소"),      // 판매자가 거래 거부
    BUYER_CANCELLED("구매자 취소"),       // 구매자 마음 변경
    MUTUAL_AGREEMENT("상호 합의"),        // 협의 후 취소
    PRODUCT_UNAVAILABLE("상품 준비 불가"), // 분실, 상품 훼손 등
    TIMEOUT("거래 시간 초과"),            // 약속한 거래 만남에 안 나옴
    ETC("기타");

    private final String description;

    RefundType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
