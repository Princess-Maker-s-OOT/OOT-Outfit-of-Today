package org.example.ootoutfitoftoday.domain.transaction.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.domain.payment.enums.EasyPayProvider;
import org.example.ootoutfitoftoday.domain.payment.enums.PaymentMethod;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class RequestTransactionRequest {

    @NotNull(message = "판매글 ID는 필수입니다.")
    private Long salePostId;

    @NotNull(message = "결제 금액은 필수입니다.")
    @DecimalMin(value = "0", message = "결제 금액은 0원 이상이어야 합니다.")
    private BigDecimal amount;

    @NotNull(message = "결제 수단은 필수입니다.")
    private PaymentMethod method;

    private EasyPayProvider easyPayProvider;  // 간편결제일 때만

    @NotBlank(message = "토스 주문 ID는 필수입니다.")
    private String tossOrderId;
}
