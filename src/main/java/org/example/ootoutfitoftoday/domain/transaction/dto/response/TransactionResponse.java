package org.example.ootoutfitoftoday.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.payment.entity.Payment;
import org.example.ootoutfitoftoday.domain.payment.enums.PaymentMethod;
import org.example.ootoutfitoftoday.domain.transaction.entity.Transaction;
import org.example.ootoutfitoftoday.domain.transaction.enums.TransactionStatus;

import java.math.BigDecimal;

@Getter
@Builder
public class TransactionResponse {

    private Long transactionId;  // 다음 API 호출용
    private String tossOrderId;  // 토스 결제창용
    private BigDecimal price;
    private TransactionStatus status;

    private String salePostTitle;

    private Long sellerId;
    private String sellerNickname;

    private PaymentMethod paymentMethod;

    public static TransactionResponse from(Transaction transaction) {
        Payment payment = transaction.getPayment();

        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .tossOrderId(payment.getTossOrderId())
                .price(transaction.getPrice())
                .status(transaction.getStatus())
                .salePostTitle(transaction.getSalePost().getTitle())
                .sellerId(transaction.getSeller().getId())
                .sellerNickname(transaction.getSeller().getNickname())
                .paymentMethod(payment.getMethod())
                .build();
    }
}
