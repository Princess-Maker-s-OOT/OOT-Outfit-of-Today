package org.example.ootoutfitoftoday.domain.transaction.service.command;

import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionConfirmRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.RequestTransactionRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionAcceptResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionCompleteResponse;
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

    /**
     * 거래 수락 (판매자)
     */
    TransactionAcceptResponse acceptTransaction(Long sellerId, Long transactionId);

    /**
     * 거래 확정 (구매자)
     */
    TransactionCompleteResponse completeTransaction(Long buyerId, Long transactionId);
}
