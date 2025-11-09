package org.example.ootoutfitoftoday.domain.transaction.service.command;

import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionConfirmRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.RequestTransactionRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionResponse;


public interface TransactionCommandService {

    /**
     * 거래 요청
     */
    TransactionResponse requestTransaction(Long userId, RequestTransactionRequest request);

    /**
     * 결제 승인
     */
    TransactionResponse confirmTransaction(
            Long userId,
            Long transactionId,
            TransactionConfirmRequest request
    );
}
