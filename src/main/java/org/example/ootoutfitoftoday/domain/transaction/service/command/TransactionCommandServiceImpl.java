package org.example.ootoutfitoftoday.domain.transaction.service.command;

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
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionConfirmRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.RequestTransactionRequest;
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
    public TransactionResponse requestTransaction(
            Long userId,
            RequestTransactionRequest request
    ) {
        // 1. 채팅방 조회
        Optional<Chatroom> chatroomOpt = chatroomQueryService
                .findByUserAndSalePost(userId, request.getSalePostId());

        if (chatroomOpt.isEmpty()) {
            throw new TransactionException(TransactionErrorCode.CHATROOM_REQUIRED_FOR_TRANSACTION);
        }

        Chatroom chatroom = chatroomOpt.get();

        // 2. 채팅 내역 확인
        boolean hasChatHistory = chatQueryService.existsByChatroom(chatroom.getId());

        if (!hasChatHistory) {
            throw new TransactionException(TransactionErrorCode.CHAT_REQUIRED_BEFORE_TRANSACTION);
        }

        // 3. 판매글 조회
        SalePost salePost = chatroom.getSalePost();

        // 4. 판매글 락으로 재조회 (동시성 제어)
        SalePost lockedSalePost = salePostRepository.findAvailableByIdForUpdate(salePost.getId())
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.SALE_POST_NOT_AVAILABLE));

        // 5. 본인 판매글 구매 방지
        if (lockedSalePost.getUser().getId().equals(userId)) {
            throw new TransactionException(TransactionErrorCode.CANNOT_BUY_OWN_POST);
        }

        // 6. 금액 검증
        if (lockedSalePost.getPrice().compareTo(request.getAmount()) != 0) {
            throw new TransactionException(TransactionErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        // 7. 중복 주문 ID 확인
        if (paymentQueryService.existsByTossOrderId(request.getTossOrderId())) {
            throw new PaymentException(PaymentErrorCode.DUPLICATE_ORDER_ID);
        }

        // 8. 진행 중인 거래 확인
        List<TransactionStatus> activeStatuses = List.of(
                TransactionStatus.PENDING_APPROVAL,
                TransactionStatus.APPROVED
        );

        if (transactionRepository.findBySalePostIdAndStatusIn(lockedSalePost.getId(), activeStatuses).isPresent()) {
            throw new TransactionException(TransactionErrorCode.ALREADY_IN_TRANSACTION);
        }

        // 9. 구매자 조회
        User buyer = userQueryService.findByIdAndIsDeletedFalse(userId);

        // 10. Transaction 생성
        Transaction transaction = Transaction.create(
                buyer,
                lockedSalePost,
                chatroom
        );
        transactionRepository.save(transaction);

        // 11. Payment 생성
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

        // 12. 응답 반환
        return TransactionResponse.from(transaction);
    }


    @Override
    @Transactional(noRollbackFor = PaymentException.class)
    public TransactionResponse confirmTransaction(
            Long userId,
            Long transactionId,
            TransactionConfirmRequest request
    ) {
        // 1. Transaction 조회
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        // 2. 권한 확인
        if (!transaction.getBuyer().getId().equals(userId)) {
            throw new TransactionException(TransactionErrorCode.UNAUTHORIZED_TRANSACTION_ACCESS);
        }

        // 3. Transaction 상태 검증
        if (transaction.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new TransactionException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        // 4. Payment 조회
        Payment payment = transaction.getPayment();
        if (payment == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        // 5. Payment 상태 검증
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        // 6. 멱등성 체크
        if (payment.getTossPaymentKey() != null) {

            return TransactionResponse.from(transaction);
        }

        // 6-1. 10분 초과 확인
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
            // 7. 토스 confirm API 호출 + 응답 받아오기
            TossConfirmResult result = tossPaymentsClient.confirmPayment(
                    request.getPaymentKey(),
                    payment.getTossOrderId(),
                    payment.getAmount()
            );

            // 8. Payment 승인
            payment.approve(
                    request.getPaymentKey(),
                    result.receiptUrl(),
                    result.approvedAt()
            );

            // 9. 판매글 상태 변경
            SalePost salePost = transaction.getSalePost();
            salePost.updateStatus(SaleStatus.RESERVED);

        } catch (PaymentException e) {
            String reason = String.format("토스 결제 승인 실패 - %s", e.getMessage());
            log.error("Payment confirmation failed - transactionId: {}, reason: {}", transactionId, reason, e);

            transaction.failPayment();
            paymentCommandService.failPayment(payment.getId(), reason);

            throw e;
        }

        // 10. 응답 반환
        return TransactionResponse.from(transaction);
    }

    @Override
    public TransactionAcceptResponse acceptTransaction(Long sellerId, Long transactionId) {

        // 1. Transaction 조회
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        // 2. 판매자 계정 확인 + 권한 확인
        if (!transaction.getSeller().getId().equals(sellerId)) {
            throw new TransactionException(TransactionErrorCode.UNAUTHORIZED_TRANSACTION_ACCESS);
        }

        // 3. Transaction 상태 검증
        if (transaction.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new TransactionException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        // 4. Payment 상태 검증
        Payment payment = transaction.getPayment();

        if (payment.getStatus() != PaymentStatus.ESCROWED) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        // 5. Transaction 상태 변경 (PENDING_APPROVAL → APPROVED)
        transaction.approve();

        // 6. SalePost 상태 변경 (RESERVED → TRADING)
        SalePost salePost = transaction.getSalePost();
        salePost.updateStatus(SaleStatus.TRADING);

        // 7. 응답 반환
        return TransactionAcceptResponse.from(transaction);
    }

    @Override
    public TransactionCompleteResponse completeTransaction(Long buyerId, Long transactionId) {

        // 1. Transaction 조회
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        // 2. 구매자 계정 확인 + 권한 확인
        if (!transaction.getBuyer().getId().equals(buyerId)) {
            throw new TransactionException(TransactionErrorCode.UNAUTHORIZED_TRANSACTION_ACCESS);
        }

        // 3. Transaction 상태 검증
        if (transaction.getStatus() != TransactionStatus.APPROVED) {
            throw new TransactionException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        // 4. Payment 조회 및 상태 검증
        Payment payment = transaction.getPayment();

        if (payment.getStatus() != PaymentStatus.ESCROWED) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        // 5. Transaction 상태 변경 (APPROVED → CONFIRMED)
        transaction.confirm();

        // 6. Payment 상태 변경 (ESCROWED → SETTLED)
        payment.settle();

        // 7. SalePost 상태 변경 (TRADING → COMPLETED)
        SalePost salePost = transaction.getSalePost();
        salePost.updateStatus(SaleStatus.COMPLETED);

        // 8. 응답 반환
        return TransactionCompleteResponse.from(transaction);
    }

    @Override
    public TransactionCancelResponse cancelByBuyer(Long buyerId, Long transactionId) {

        // 1. Transaction 조회
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        // 2. 구매자 본인 확인
        if (!transaction.getBuyer().getId().equals(buyerId)) {
            throw new TransactionException(TransactionErrorCode.UNAUTHORIZED_TRANSACTION_ACCESS);
        }

        // 3. Transaction 상태 검증
        if (transaction.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new TransactionException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        // 4. Payment 조회 및 상태 검증
        Payment payment = transaction.getPayment();

        if (payment.getStatus() != PaymentStatus.ESCROWED) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        // 5. Transaction 상태 변경 (PENDING_APPROVAL → CANCELLED_BY_BUYER)
        transaction.cancelByBuyer();

        // 6. Payment 상태 변경 (ESCROWED → REFUNDED)
        payment.refundByBuyer();

        // 7. SalePost 상태 변경 (RESERVED → AVAILABLE)
        SalePost salePost = transaction.getSalePost();
        salePost.updateStatus(SaleStatus.AVAILABLE);

        // 8. 응답 반환
        return TransactionCancelResponse.from(transaction);
    }
}
