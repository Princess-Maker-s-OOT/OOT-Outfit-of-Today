package org.example.ootoutfitoftoday.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.payment.enums.EasyPayProvider;
import org.example.ootoutfitoftoday.domain.payment.enums.PaymentMethod;
import org.example.ootoutfitoftoday.domain.payment.enums.PaymentStatus;
import org.example.ootoutfitoftoday.domain.payment.enums.RefundType;
import org.example.ootoutfitoftoday.domain.transaction.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === 결제 기본 정보 ===

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amount;  // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;  // 결제 수단

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;  // 결제 상태

    // === 토스 연동 정보 ===

    @Column(length = 36, nullable = false, unique = true)
    private String tossOrderId;  // 토스 주문 ID (UUID)

    @Column(length = 200)
    private String tossPaymentKey;  // 토스 결제 키 (결제 승인 후 생성됨)

    // === 간편결제 정보 ===

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EasyPayProvider easyPayProvider;  // 간편결제 제공자

    // === 결제 시간 정보 ===

    private LocalDateTime requestedAt;  // 요청
    private LocalDateTime approvedAt;  // 승인 (토스 승인)
    private LocalDateTime settledAt;  // 정산

    // === 환불(반품 미포함) 정보 ===

    private LocalDateTime refundedAt;  // 환불 시각

    @Column(precision = 12, scale = 0)
    private BigDecimal refundedAmount = BigDecimal.ZERO;  // 환불 금액

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RefundType refundType;  // 환불 유형

    @Column(length = 500)
    private String refundReason;  // 상세 사유 (선택)

    // === PG(결제 대행사) 응답 정보 ===

    @Column(length = 500)
    private String receiptUrl;  // 영수증 URL

    // === 연관 관계 ===

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;  // 거래

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(
            BigDecimal amount,
            PaymentMethod method,
            PaymentStatus status,
            String tossOrderId,
            EasyPayProvider easyPayProvider
    ) {
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.tossOrderId = tossOrderId;
        this.easyPayProvider = easyPayProvider;
    }

    // 계좌이체 결제 생성
    public static Payment createAccountTransfer(
            Transaction transaction,
            BigDecimal amount,
            String tossOrderId
    ) {
        Payment payment = Payment.builder()
                .amount(amount)
                .method(PaymentMethod.ACCOUNT_TRANSFER)
                .status(PaymentStatus.PENDING)
                .tossOrderId(tossOrderId)
                .build();
        return payment.attachTo(transaction);
    }

    // 간편결제 생성
    public static Payment createEasyPay(
            Transaction transaction,
            BigDecimal amount,
            String tossOrderId,
            EasyPayProvider easyPayProvider
    ) {
        Payment payment = Payment.builder()
                .amount(amount)
                .method(PaymentMethod.EASY_PAY)
                .status(PaymentStatus.PENDING)
                .tossOrderId(tossOrderId)
                .easyPayProvider(easyPayProvider)
                .build();
        return payment.attachTo(transaction);
    }

    @PrePersist
    void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }

    // 양방향 연관관계 편의 메서드
    public Payment attachTo(Transaction tx) {
        this.transaction = tx;
        tx.setPayment(this);
        return this;
    }

    public void approve(
            String tossPaymentKey,
            String receiptUrl,
            LocalDateTime approvedAt
    ) {
        this.tossPaymentKey = tossPaymentKey;
        this.receiptUrl = receiptUrl;
        this.approvedAt = approvedAt;
        this.status = PaymentStatus.ESCROWED;
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }
}
