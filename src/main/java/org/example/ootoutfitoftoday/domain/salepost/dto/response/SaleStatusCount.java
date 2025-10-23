package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;

@Getter
@AllArgsConstructor
public class SaleStatusCount {

    private final SaleStatus saleStatus;
    private final long count;
}