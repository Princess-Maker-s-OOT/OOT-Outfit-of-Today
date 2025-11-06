package org.example.ootoutfitoftoday.domain.transaction.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.client.payment.TossPaymentsClient;
import org.example.ootoutfitoftoday.domain.chat.service.query.ChatQueryService;
import org.example.ootoutfitoftoday.domain.chatroom.entity.Chatroom;
import org.example.ootoutfitoftoday.domain.chatroom.service.query.ChatroomQueryService;
import org.example.ootoutfitoftoday.domain.payment.entity.Payment;
import org.example.ootoutfitoftoday.domain.payment.enums.PaymentMethod;
import org.example.ootoutfitoftoday.domain.payment.enums.PaymentStatus;
import org.example.ootoutfitoftoday.domain.payment.exception.PaymentErrorCode;
import org.example.ootoutfitoftoday.domain.payment.exception.PaymentException;
import org.example.ootoutfitoftoday.domain.payment.repository.PaymentRepository;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.ConfirmTransactionRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.RequestTransactionRequest;
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

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionCommandServiceImpl implements TransactionCommandService {

    private final SalePostRepository salePostRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final UserQueryService userQueryService;
    private final ChatroomQueryService chatroomQueryService;
    private final ChatQueryService chatQueryService;
    private final TossPaymentsClient tossPaymentsClient;

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
        if (paymentRepository.findByTossOrderId(request.getTossOrderId()).isPresent()) {
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

        paymentRepository.save(payment);

        // 12. 응답 반환
        return TransactionResponse.from(transaction);
    }


    @Override
    public TransactionResponse confirmTransaction(
            Long userId,
            Long transactionId,
            ConfirmTransactionRequest request
    ) {
        // 1. Transaction 조회
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        // 2. 권한 확인
        if (!transaction.getBuyer().getId().equals(userId)) {
            throw new TransactionException(TransactionErrorCode.UNAUTHORIZED_TRANSACTION_ACCESS);
        }

        // 3. 상태 검증 (오승인 방지)
        if (transaction.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new TransactionException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        // 4. Payment 조회
        Payment payment = transaction.getPayment();
        if (payment == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        // 5. 결제 상태 검증
        if (payment.getStatus() != PaymentStatus.ESCROWED) {
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
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRMATION_TIMEOUT);
        }

        // 7. 토스 confirm API 호출
        tossPaymentsClient.confirmPayment(
                    request.getPaymentKey(),
                    payment.getTossOrderId(),
                    payment.getAmount()
        );

        // 8. Payment 승인
        payment.approve(request.getPaymentKey());

        // 9. 판매글 상태 변경
        SalePost salePost = transaction.getSalePost();
        salePost.updateStatus(SaleStatus.RESERVED);

        // 10. 응답
        return TransactionResponse.from(transaction);
    }
}
