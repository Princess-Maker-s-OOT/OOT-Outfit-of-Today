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

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 36, nullable = false, unique = true)
    private String tossOrderId;

    @Column(length = 200)
    private String tossPaymentKey;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EasyPayProvider easyPayProvider;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime settledAt;

    private LocalDateTime refundedAt;

    @Column(precision = 12, scale = 0)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RefundType refundType;

    @Column(length = 500)
    private String refundReason;

    @Column(length = 500)
    private String receiptUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

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

    public void settle() {
        this.status = PaymentStatus.SETTLED;
        this.settledAt = LocalDateTime.now();
    }

    public void refundByBuyer() {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
        this.refundedAmount = this.amount;
        this.refundType = RefundType.BUYER_CANCELLED;
    }
}