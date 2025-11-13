package org.example.ootoutfitoftoday.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.transaction.entity.Transaction;
import org.example.ootoutfitoftoday.domain.transaction.enums.TransactionStatus;

import java.math.BigDecimal;

@Getter
@Builder
public class TransactionAcceptResponse {

    private Long transactionId;
    private BigDecimal price;
    private TransactionStatus status;
    private String salePostTitle;
    private Long buyerId;
    private String buyerNickname;

    public static TransactionAcceptResponse from(Transaction transaction) {

        return TransactionAcceptResponse.builder()
                .transactionId(transaction.getId())
                .price(transaction.getPrice())
                .status(transaction.getStatus())
                .salePostTitle(transaction.getSalePost().getTitle())
                .buyerId(transaction.getBuyer().getId())
                .buyerNickname(transaction.getBuyer().getNickname())
                .build();
    }
}
