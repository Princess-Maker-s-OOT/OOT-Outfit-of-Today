package org.example.ootoutfitoftoday.domain.salepost.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePostImage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SalePostDetailResponse {

    // 판매글 정보
    private final Long salePostId;
    private final String title;
    private final String content;
    private final BigDecimal price;
    private final String status;

    // 판매자 정보
    private final Long sellerId;
    private final String sellerNickname;
    private final String sellerImageUrl;

    // 카테고리 정보
    private final String categoryName;

    // 이미지 리스트
    private final List<String> imageUrls;

    // 시간 정보
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static SalePostDetailResponse from(SalePost salePost) {

        return SalePostDetailResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .content(salePost.getContent())
                .price(salePost.getPrice())
                .status(salePost.getStatus().name())
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
