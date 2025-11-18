package org.example.ootoutfitoftoday.domain.transaction.service.command;

import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionConfirmRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionAcceptResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionCancelResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionCompleteResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionResponse;


public interface TransactionCommandService {

    TransactionResponse requestTransaction(Long userId, TransactionRequest request);

    TransactionResponse confirmTransaction(
            Long userId,
            Long transactionId,
            TransactionConfirmRequest request
    );

    TransactionAcceptResponse acceptTransaction(Long sellerId, Long transactionId);

    TransactionCompleteResponse completeTransaction(Long buyerId, Long transactionId);

    TransactionCancelResponse cancelByBuyer(Long buyerId, Long transactionId);
}
