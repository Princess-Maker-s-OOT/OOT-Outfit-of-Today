package org.example.ootoutfitoftoday.domain.transaction.service.command;

import com.ootcommon.salepost.enums.SaleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.Toss.client.TossPaymentsClient;
import org.example.ootoutfitoftoday.Toss.dto.TossConfirmResult;
import org.example.ootoutfitoftoday.domain.chat.service.query.ChatQueryService;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryService;
import org.example.ootoutfitoftoday.domain.payment.entity.Payment;
import org.example.ootoutfitoftoday.domain.payment.enums.PaymentMethod;
import org.example.ootoutfitoftoday.domain.payment.enums.PaymentStatus;
import org.example.ootoutfitoftoday.domain.payment.exception.PaymentErrorCode;
import org.example.ootoutfitoftoday.domain.payment.exception.PaymentException;
import org.example.ootoutfitoftoday.domain.payment.service.command.PaymentCommandService;
import org.example.ootoutfitoftoday.domain.payment.service.query.PaymentQueryService;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionConfirmRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionAcceptResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionCancelResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionCompleteResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionResponse;
import org.example.ootoutfitoftoday.domain.transaction.entity.Transaction;
import org.example.ootoutfitoftoday.domain.transaction.enums.TransactionStatus;
import org.example.ootoutfitoftoday.domain.transaction.exception.TransactionErrorCode;
import org.example.ootoutfitoftoday.domain.transaction.exception.TransactionException;
import org.example.ootoutfitoftoday.domain.transaction.repository.TransactionRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionCommandServiceImpl implements TransactionCommandService {

    private final SalePostRepository salePostRepository;
    private final TransactionRepository transactionRepository;
    private final UserQueryService userQueryService;
    private final ChatroomQueryService chatroomQueryService;
    private final ChatQueryService chatQueryService;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    @Override
    public TransactionResponse requestTransaction(Long userId, TransactionRequest request) {
        Optional<Chatroom> chatroomOpt = chatroomQueryService
                .findByUserAndSalePost(userId, request.getSalePostId());

        if (chatroomOpt.isEmpty()) {
            throw new TransactionException(TransactionErrorCode.CHATROOM_REQUIRED_FOR_TRANSACTION);
        }

        Chatroom chatroom = chatroomOpt.get();

        boolean hasChatHistory = chatQueryService.existsByChatroom(chatroom.getId());

        if (!hasChatHistory) {
            throw new TransactionException(TransactionErrorCode.CHAT_REQUIRED_BEFORE_TRANSACTION);
        }

        SalePost salePost = chatroom.getSalePost();

        SalePost lockedSalePost = salePostRepository.findAvailableByIdForUpdate(salePost.getId())
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.SALE_POST_NOT_AVAILABLE));

        if (lockedSalePost.getUser().getId().equals(userId)) {
            throw new TransactionException(TransactionErrorCode.CANNOT_BUY_OWN_POST);
        }

        if (lockedSalePost.getPrice().compareTo(request.getAmount()) != 0) {
            throw new TransactionException(TransactionErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        if (paymentQueryService.existsByTossOrderId(request.getTossOrderId())) {
            throw new PaymentException(PaymentErrorCode.DUPLICATE_ORDER_ID);
        }

        List<TransactionStatus> activeStatuses = List.of(
                TransactionStatus.PENDING_APPROVAL,
                TransactionStatus.APPROVED
        );

        Optional<Transaction> existingTransaction = transactionRepository.findActiveBySalePostIdForUpdate(
                lockedSalePost.getId(),
                activeStatuses
        );

        if (existingTransaction.isPresent()) {
            throw new TransactionException(TransactionErrorCode.ALREADY_IN_TRANSACTION);
        }

        User buyer = userQueryService.findByIdAndIsDeletedFalse(userId);

        Transaction transaction = Transaction.create(
                buyer,
                lockedSalePost,
                chatroom
        );
        transactionRepository.save(transaction);

        Payment payment;

        if (request.getMethod() == PaymentMethod.EASY_PAY) {

            if (request.getEasyPayProvider() == null) {
                throw new PaymentException(PaymentErrorCode.EASY_PAY_PROVIDER_REQUIRED);
            }

            payment = Payment.createEasyPay(
                    transaction,
                    request.getAmount(),
                    request.getTossOrderId(),
                    request.getEasyPayProvider()
            );
        } else {
            payment = Payment.createAccountTransfer(
                    transaction,
                    request.getAmount(),
                    request.getTossOrderId()
            );
        }

        paymentCommandService.savePayment(payment);

        return TransactionResponse.from(transaction);
    }

    @Override
    @Transactional(noRollbackFor = PaymentException.class)
    public TransactionResponse confirmTransaction(
            Long userId,
            Long transactionId,
            TransactionConfirmRequest request
    ) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        if (!transaction.getBuyer().getId().equals(userId)) {
            throw new TransactionException(TransactionErrorCode.UNAUTHORIZED_TRANSACTION_ACCESS);
        }

        if (transaction.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new TransactionException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        Payment payment = transaction.getPayment();
        if (payment == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        if (payment.getTossPaymentKey() != null) {

            return TransactionResponse.from(transaction);
        }

        LocalDateTime createdAt = transaction.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();

        if (Duration.between(createdAt, now).toMinutes() > 10) {
            String reason = String.format("결제 승인 타임아웃 - 생성시간: %s, 현재시간: %s", createdAt, now);
            log.warn("Transaction expired - transactionId: {}, reason: {}", transactionId, reason);

            transaction.expire();
            paymentCommandService.failPayment(payment.getId(), reason);

            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRMATION_TIMEOUT);
        }

        try {
            TossConfirmResult result = tossPaymentsClient.confirmPayment(
                    request.getPaymentKey(),
                    payment.getTossOrderId(),
                    payment.getAmount()
            );

            payment.approve(
                    request.getPaymentKey(),
                    result.receiptUrl(),
                    result.approvedAt()
            );

            SalePost salePost = transaction.getSalePost();
            salePost.updateStatus(SaleStatus.RESERVED);

        } catch (PaymentException e) {
            String reason = String.format("토스 결제 승인 실패 - %s", e.getMessage());
            log.error("Payment confirmation failed - transactionId: {}, reason: {}", transactionId, reason, e);

            transaction.failPayment();
            paymentCommandService.failPayment(payment.getId(), reason);

            throw e;
        }

        return TransactionResponse.from(transaction);
    }

    @Override
    public TransactionAcceptResponse acceptTransaction(Long sellerId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        if (!transaction.getSeller().getId().equals(sellerId)) {
            throw new TransactionException(TransactionErrorCode.UNAUTHORIZED_TRANSACTION_ACCESS);
        }

        if (transaction.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new TransactionException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        Payment payment = transaction.getPayment();

        if (payment.getStatus() != PaymentStatus.ESCROWED) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        transaction.approve();

        SalePost salePost = transaction.getSalePost();
        salePost.updateStatus(SaleStatus.TRADING);

        return TransactionAcceptResponse.from(transaction);
    }

    @Override
    public TransactionCompleteResponse completeTransaction(Long buyerId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        if (!transaction.getBuyer().getId().equals(buyerId)) {
            throw new TransactionException(TransactionErrorCode.UNAUTHORIZED_TRANSACTION_ACCESS);
        }

        if (transaction.getStatus() != TransactionStatus.APPROVED) {
            throw new TransactionException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        Payment payment = transaction.getPayment();

        if (payment.getStatus() != PaymentStatus.ESCROWED) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        transaction.confirm();

        payment.settle();

        SalePost salePost = transaction.getSalePost();
        salePost.updateStatus(SaleStatus.COMPLETED);

        return TransactionCompleteResponse.from(transaction);
    }

    @Override
    public TransactionCancelResponse cancelByBuyer(Long buyerId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        if (!transaction.getBuyer().getId().equals(buyerId)) {
            throw new TransactionException(TransactionErrorCode.UNAUTHORIZED_TRANSACTION_ACCESS);
        }

        if (transaction.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new TransactionException(TransactionErrorCode.TRANSACTION_NOT_CANCELLABLE);
        }

        Payment payment = transaction.getPayment();

        if (payment.getStatus() != PaymentStatus.ESCROWED) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_REFUNDABLE);
        }

        transaction.cancelByBuyer();

        payment.refundByBuyer();

        SalePost salePost = transaction.getSalePost();
        salePost.updateStatus(SaleStatus.AVAILABLE);

        return TransactionCancelResponse.from(transaction);
    }
}