package org.example.ootoutfitoftoday.domain.salepost.enums;

public enum SaleStatus {
    AVAILABLE,   // 판매중 (결제 가능)
    RESERVED,    // 예약됨 (결제 완료, 수락 대기)
    TRADING,     // 거래중 (판매자 수락 완료)
    COMPLETED,   // 거래 완료
    CANCELLED,   // 취소됨
    DELETED      // 삭제됨
}

// description 추가 시

//    AVAILABLE("판매중"),
//    RESERVED("예약됨"),
//    TRADING("거래중"),
//    COMPLETED("거래 완료"),
//    CANCELLED("취소됨"),
//    DELETED("삭제됨");
//
//    private final String description;
//
//    SalePostStatus(String description) {
//        this.description = description;
//    }
//
//    public String getDescription() {
//        return description;
//    }
