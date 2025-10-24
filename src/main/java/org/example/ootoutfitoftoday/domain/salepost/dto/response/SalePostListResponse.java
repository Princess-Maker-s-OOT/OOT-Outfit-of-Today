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

    private final Long salePostId;
    private final String title;
    private final BigDecimal price;
    private final SaleStatus status;
    private final String thumbnailUrl;
    private final String sellerNickname;
    private final String categoryName;
    private final LocalDateTime createdAt;

    public static SalePostListResponse from(SalePost salePost) {

        return SalePostListResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .price(salePost.getPrice())
                .status(salePost.getStatus())
                .thumbnailUrl(getThumbnailUrl(salePost))
                .sellerNickname(salePost.getUser().getNickname())
                .categoryName(salePost.getCategory().getName())
                .createdAt(salePost.getCreatedAt())
                .build();
    }

    private static String getThumbnailUrl(SalePost salePost) {
        if (salePost.getImages() == null || salePost.getImages().isEmpty()) {
            return null;
        }

        return salePost.getImages().get(0).getImageUrl();
    }
}
