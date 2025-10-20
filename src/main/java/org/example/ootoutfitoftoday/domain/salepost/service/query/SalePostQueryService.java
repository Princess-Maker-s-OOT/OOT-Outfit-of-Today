package org.example.ootoutfitoftoday.domain.salepost.service.query;

import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostDetailResponse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;

public interface SalePostQueryService {

    SalePost findSalePostById(Long salePostId);

    SalePostDetailResponse getSalePostDetail(Long salePostId);
}
