package org.example.ootoutfitoftoday.domain.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.payment.exception.PaymentSuccessCode;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionConfirmRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.RequestTransactionRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionAcceptResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionResponse;
import org.example.ootoutfitoftoday.domain.transaction.exception.TransactionSuccessCode;
import org.example.ootoutfitoftoday.domain.transaction.service.command.TransactionCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "거래 관리", description = "거래 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionCommandService transactionCommandService;

    @Operation(
            summary = "거래 요청",
            description = "판매글에 대한 거래를 요청하고 결제 정보를 생성합니다."
//            responses = {}
    )
    @PostMapping("/request")
    public ResponseEntity<Response<TransactionResponse>> requestTransaction(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody RequestTransactionRequest request
    ) {
        TransactionResponse response = transactionCommandService.requestTransaction(
                authUser.getUserId(),
                request
        );

        return Response.success(response, TransactionSuccessCode.TRANSACTION_REQUESTED);
    }

    @Operation(
            summary = "결제 승인",
            description = "토스페이먼츠 결제를 승인하고 paymentKey를 저장합니다."
//            responses = {}
    )
    @PostMapping("/{transactionId}/confirm")
    public ResponseEntity<Response<TransactionResponse>> confirmTransaction(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long transactionId,
            @Valid @RequestBody TransactionConfirmRequest request
    ) {
        TransactionResponse response = transactionCommandService.confirmTransaction(
                authUser.getUserId(),
                transactionId,
                request
        );

        return Response.success(response, PaymentSuccessCode.PAYMENT_APPROVED);
    }

    @Operation(
            summary = "거래 수락",
            description = "판매자가 거래를 수락합니다."
//            responses = {}
    )
    @PostMapping("/{transactionId}/accept")
    public ResponseEntity<Response<TransactionAcceptResponse>> acceptTransaction(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long transactionId
    ) {
        TransactionAcceptResponse response = transactionCommandService.acceptTransaction(
                authUser.getUserId(),
                transactionId
        );

        return Response.success(response, TransactionSuccessCode.TRANSACTION_ACCEPTED);
    }
}
