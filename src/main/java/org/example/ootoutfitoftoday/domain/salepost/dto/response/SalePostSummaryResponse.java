package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import com.ootcommon.salepost.enums.SaleStatus;
import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.util.Location;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SalePostSummaryResponse {

    private final Long salePostId;
    private final String title;
    private final BigDecimal price;
    private final SaleStatus status;
    private final String tradeAddress;
    private final BigDecimal tradeLatitude;
    private final BigDecimal tradeLongitude;
    private final String thumbnailUrl;
    private final LocalDateTime createdAt;

    public static SalePostSummaryResponse from(SalePost salePost) {
        Location location = PointFormatAndParse.parse(salePost.getTradeLocation());

        return SalePostSummaryResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .price(salePost.getPrice())
                .status(salePost.getStatus())
                .tradeAddress(salePost.getTradeAddress())
                .tradeLatitude(location.latitude())
                .tradeLongitude(location.longitude())
                .thumbnailUrl(getThumbnailUrl(salePost))
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
