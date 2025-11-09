package org.example.ootoutfitoftoday.domain.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TransactionConfirmRequest {

    @NotBlank(message = "결제 키는 필수입니다.")
    private String paymentKey;
}
