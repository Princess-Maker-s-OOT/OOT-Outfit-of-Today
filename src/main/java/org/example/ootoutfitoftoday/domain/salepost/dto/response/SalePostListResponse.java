package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SalePostListResponse {

    // 판매글 기본 정보
    private final Long salePostId;
    private final String title;
    private final BigDecimal price;
    private final SaleStatus status;

    // 썸네일 (첫 번째 이미지)
    private final String thumbnailUrl;

    // 판매자 정보
    private final String sellerNickname;

    // 카테고리
    private final String categoryName;

    private final LocalDateTime createdAt;

    public static SalePostListResponse from(SalePost salePost) {

        return SalePostListResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .price(salePost.getPrice())
                .status(salePost.getStatus())
                .thumbnailUrl(salePost.getImages().isEmpty()
                        ? null
                        : salePost.getImages().get(0).getImageUrl())
                .sellerNickname(salePost.getUser().getNickname())
                .categoryName(salePost.getCategory().getName())
                .createdAt(salePost.getCreatedAt())
                .build();
    }
}
