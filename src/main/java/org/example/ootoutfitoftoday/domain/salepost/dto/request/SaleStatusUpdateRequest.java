package org.example.ootoutfitoftoday.domain.salepost.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;

@Getter
@NoArgsConstructor
public class SaleStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다.")
    private SaleStatus status;
}
