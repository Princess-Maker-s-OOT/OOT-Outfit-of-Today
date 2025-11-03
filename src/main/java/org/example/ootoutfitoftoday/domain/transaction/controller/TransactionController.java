package org.example.ootoutfitoftoday.domain.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.RequestTransactionRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionResponse;
import org.example.ootoutfitoftoday.domain.transaction.exception.TransactionSuccessCode;
import org.example.ootoutfitoftoday.domain.transaction.service.command.TransactionCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionCommandService transactionCommandService;

    @Operation(
            summary = "거래 요청",
            description = "판매글에 대한 거래를 요청하고 결제 정보를 생성합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
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
}
