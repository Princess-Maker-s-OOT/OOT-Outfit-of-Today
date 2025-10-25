package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePostImage;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SalePostCreateResponse {

    private final Long salePostId;
    private final String title;
    private final String content;
    private final BigDecimal price;
    private final SaleStatus status;
    private final String tradeAddress;
    private final String tradeLocation;
    private final Long userId;
    private final Long categoryId;
    private final List<String> imageUrls;
    private final LocalDateTime createdAt;

    public static SalePostCreateResponse from(SalePost salePost) {

        return SalePostCreateResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .content(salePost.getContent())
                .price(salePost.getPrice())
                .status(salePost.getStatus())
                .tradeAddress(salePost.getTradeAddress())
                .tradeLocation(salePost.getTradeLocation())
                .userId(salePost.getUser().getId())
                .categoryId(salePost.getCategory().getId())
                .imageUrls(salePost.getImages().stream()
                        .map(SalePostImage::getImageUrl)
                        .toList())
                .createdAt(salePost.getCreatedAt())
                .build();
    }
}
