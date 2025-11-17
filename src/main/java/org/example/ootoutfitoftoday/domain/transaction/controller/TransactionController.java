package org.example.ootoutfitoftoday.domain.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.payment.exception.PaymentSuccessCode;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionConfirmRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.request.TransactionRequest;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionAcceptResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionCancelResponse;
import org.example.ootoutfitoftoday.domain.transaction.dto.response.TransactionCompleteResponse;
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
            description = "판매글에 대한 거래를 요청하고 결제 정보를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "요청 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "409", description = "리소스 충돌")
            }
    )
    @PostMapping("/request")
    public ResponseEntity<Response<TransactionResponse>> requestTransaction(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionCommandService.requestTransaction(
                authUser.getUserId(),
                request
        );

        return Response.success(response, TransactionSuccessCode.TRANSACTION_REQUESTED);
    }

    @Operation(
            summary = "결제 승인",
            description = "토스페이먼츠 결제를 승인하고 paymentKey를 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "승인 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음"),
                    @ApiResponse(responseCode = "408", description = "요청 시간 초과")
            }
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
            description = "판매자가 거래를 수락합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수락 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
            }
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

    @Operation(
            summary = "거래 확정",
            description = "구매자가 물건을 받은 후 거래를 확정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "확정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
            }
    )
    @PostMapping("/{transactionId}/complete")
    public ResponseEntity<Response<TransactionCompleteResponse>> completeTransaction(
            @PathVariable Long transactionId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        TransactionCompleteResponse response = transactionCommandService.completeTransaction(
                authUser.getUserId(),
                transactionId
        );

        return Response.success(response, TransactionSuccessCode.TRANSACTION_COMPLETED);
    }

    @Operation(
            summary = "구매자 취소",
            description = "판매자 수락 이전에 구매자가 거래를 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "취소 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
            }
    )
    @PostMapping("/{transactionId}/cancel-buyer")
    public ResponseEntity<Response<TransactionCancelResponse>> cancelByBuyer(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long transactionId
    ) {
        TransactionCancelResponse response = transactionCommandService.cancelByBuyer(
                authUser.getUserId(),
                transactionId
        );

        return Response.success(response, TransactionSuccessCode.TRANSACTION_CANCELLED);
    }
}
