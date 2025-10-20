package org.example.ootoutfitoftoday.domain.salepost.service.command;

import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;

import java.util.List;

public interface SalePostCommandService {

    // 1. 판매글 생성
    SalePostCreateResponse createSalePost(
            Long userId,
            SalePostCreateRequest request,
            List<String> imageUrls
    );
}
