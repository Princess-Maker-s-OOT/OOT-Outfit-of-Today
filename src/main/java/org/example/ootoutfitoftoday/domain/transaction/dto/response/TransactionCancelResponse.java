package org.example.ootoutfitoftoday.domain.transaction.dto.response;

import com.ootcommon.salepost.enums.SaleStatus;
import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.payment.enums.PaymentStatus;
import org.example.ootoutfitoftoday.domain.transaction.entity.Transaction;
import org.example.ootoutfitoftoday.domain.transaction.enums.TransactionStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionCancelResponse {

    private Long transactionId;
    private TransactionStatus status;
    private LocalDateTime cancelRequestedAt;
    private PaymentStatus paymentStatus;
    private SaleStatus salePostStatus;

    public static TransactionCancelResponse from(Transaction transaction) {

        return TransactionCancelResponse.builder()
                .transactionId(transaction.getId())
                .status(transaction.getStatus())
                .cancelRequestedAt(transaction.getCancelRequestedAt())
                .paymentStatus(transaction.getPayment().getStatus())
                .salePostStatus(transaction.getSalePost().getStatus())
                .build();
    }
}
