package org.example.ootoutfitoftoday.domain.transaction.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.payment.entity.Payment;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.transaction.enums.TransactionStatus;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === 거래 기본 정보 ===

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;  // 거래 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;  // 거래 상태

    // === 거래 진행 정보 ===

    private LocalDateTime approvedAt;  // 판매자 수락 시각
    private LocalDateTime confirmedAt;  // 구매 확정 시각
    private LocalDateTime cancelRequestedAt;  // 취소 요청 시각 (수락 후)

    // === 취소/환불 사유 ===

    @Column(length = 500)
    private String cancelReason;  // 취소 사유

    // === 연관 관계 ===

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;  // 구매자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_post_id", nullable = false)
    private SalePost salePost;  // 판매글

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private Chatroom chatRoom;  // 채팅방

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;  // 결제 정보

    @Builder(access = AccessLevel.PRIVATE)
    private Transaction(
            User buyer,
            SalePost salePost,
            Chatroom chatRoom,
            BigDecimal price
    ) {
        this.buyer = buyer;
        this.salePost = salePost;
        this.chatRoom = chatRoom;
        this.price = price;
        this.status = TransactionStatus.PENDING_APPROVAL;
    }

    public static Transaction create(
            User buyer,
            SalePost salePost,
            Chatroom chatRoom
    ) {
        return Transaction.builder()
                .buyer(buyer)
                .salePost(salePost)
                .chatRoom(chatRoom)
                .price(salePost.getPrice())
                .build();
    }

    public void setPayment(Payment p) {
        this.payment = p;
    }

    public User getSeller() {
        return salePost.getSeller();
    }

    public void failPayment() {
        this.status = TransactionStatus.PAYMENT_FAILED;
    }

    public void expire() {
        this.status = TransactionStatus.EXPIRED;
    }

    public void approve() {
        this.status = TransactionStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void confirm() {
        this.status = TransactionStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }
}