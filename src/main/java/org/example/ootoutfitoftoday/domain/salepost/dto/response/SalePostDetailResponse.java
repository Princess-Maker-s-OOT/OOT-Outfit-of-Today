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
public class SalePostDetailResponse {

    private final Long salePostId;
    private final String title;
    private final String content;
    private final BigDecimal price;
    private final SaleStatus status;
    private final Long sellerId;
    private final String sellerNickname;
    private final String sellerImageUrl;
    private final String categoryName;
    private final List<String> imageUrls;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static SalePostDetailResponse from(SalePost salePost) {

        return SalePostDetailResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .content(salePost.getContent())
                .price(salePost.getPrice())
                .status(salePost.getStatus())
                .sellerId(salePost.getUser().getId())
                .sellerNickname(salePost.getUser().getNickname())
                .sellerImageUrl(salePost.getUser().getImageUrl())
                .categoryName(salePost.getCategory().getName())
                .imageUrls(salePost.getImages().stream()
                        .map(SalePostImage::getImageUrl)
                        .toList())
                .createdAt(salePost.getCreatedAt())
                .updatedAt(salePost.getUpdatedAt())
                .build();
    }
}
