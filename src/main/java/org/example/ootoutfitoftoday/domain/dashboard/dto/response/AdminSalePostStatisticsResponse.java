package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.NewSalePost;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SaleStatusCount;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminSalePostStatisticsResponse {

    private final long totalSales;
    private final List<SaleStatusCount> salePostStatusCounts;
    private final NewSalePost newSalePost;
}
