package org.example.ootoutfitoftoday.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.transaction.entity.Transaction;
import org.example.ootoutfitoftoday.domain.transaction.enums.TransactionStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionCompleteResponse {

    private Long transactionId;
    private TransactionStatus status;
    private LocalDateTime confirmedAt;

    public static TransactionCompleteResponse from(Transaction transaction) {

        return TransactionCompleteResponse.builder()
                .transactionId(transaction.getId())
                .status(transaction.getStatus())
                .confirmedAt(transaction.getConfirmedAt())
                .build();
    }
}
