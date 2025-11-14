package com.ootcommon.dashboard.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ootcommon.salepost.response.NewSalePost;
import com.ootcommon.salepost.response.SaleStatusCount;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class AdminSalePostStatisticsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long totalSales;
    private final List<SaleStatusCount> salePostStatusCounts;
    private final NewSalePost newSalePost;

    @JsonCreator
    public AdminSalePostStatisticsResponse(
            @JsonProperty("totalSales") long totalSales,
            @JsonProperty("salePostStatusCounts") List<SaleStatusCount> salePostStatusCounts,
            @JsonProperty("newSalePost") NewSalePost newSalePost
    ) {
        this.totalSales = totalSales;
        this.salePostStatusCounts = salePostStatusCounts;
        this.newSalePost = newSalePost;
    }
}
