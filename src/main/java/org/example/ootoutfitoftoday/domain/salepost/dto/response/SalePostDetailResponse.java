package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import com.ootcommon.salepost.enums.SaleStatus;
import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.util.Location;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;

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
    private final String tradeAddress;
    private final BigDecimal tradeLatitude;
    private final BigDecimal tradeLongitude;
    private final Long sellerId;
    private final String sellerNickname;
    private final String sellerImageUrl;
    private final String categoryName;
    private final List<SalePostImageResponse> images;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static SalePostDetailResponse from(SalePost salePost) {
        Location location = PointFormatAndParse.parse(salePost.getTradeLocation());

        return SalePostDetailResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .content(salePost.getContent())
                .price(salePost.getPrice())
                .status(salePost.getStatus())
                .tradeAddress(salePost.getTradeAddress())
                .tradeLatitude(location.latitude())
                .tradeLongitude(location.longitude())
                .sellerId(salePost.getUser().getId())
                .sellerNickname(salePost.getUser().getNickname())
                .sellerImageUrl(salePost.getUser().getImageUrl())
                .categoryName(salePost.getCategory().getName())
                .images(salePost.getImages().stream()
                        .map(SalePostImageResponse::from)
                        .toList())
                .createdAt(salePost.getCreatedAt())
                .updatedAt(salePost.getUpdatedAt())
                .build();
    }
}
