package org.example.ootoutfitoftoday.domain.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.NewSalePost;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SaleStatusCount;

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
