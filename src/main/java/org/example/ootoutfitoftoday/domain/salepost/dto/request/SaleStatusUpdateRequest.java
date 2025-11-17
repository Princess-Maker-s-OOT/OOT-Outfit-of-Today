package org.example.ootoutfitoftoday.domain.salepost.dto.request;

import com.ootcommon.salepost.enums.SaleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SaleStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다.")
    private SaleStatus status;
}
