package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewSalePost {

    private final long dailySales;
    private final long weeklySales;
    private final long monthlySales;
}
