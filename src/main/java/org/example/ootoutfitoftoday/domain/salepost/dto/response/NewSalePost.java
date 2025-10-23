package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewSalePost {

    private final long daily;
    private final long weekly;
    private final long monthly;
}
