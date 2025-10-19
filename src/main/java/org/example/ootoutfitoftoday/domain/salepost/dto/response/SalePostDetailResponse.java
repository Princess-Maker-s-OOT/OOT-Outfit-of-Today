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
    private Long salePostId;
    private String title;
    private String content;
    private BigDecimal price;
    private String status;

    // 판매자 정보
    private Long sellerId;
    private String sellerNickname;
    private String sellerProfileImage;

    // 카테고리 정보
    private String categoryName;

    // 이미지 리스트
    private List<String> imageUrls;

    // 시간 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SalePostDetailResponse from(SalePost salePost) {

        return SalePostDetailResponse.builder()
                .salePostId(salePost.getId())
                .title(salePost.getTitle())
                .content(salePost.getContent())
                .price(salePost.getPrice())
                .status(salePost.getStatus().name())
                .sellerId(salePost.getUser().getId())
                .sellerNickname(salePost.getUser().getNickname())
                .sellerProfileImage(salePost.getUser().getImageUrl())
                .categoryName(salePost.getCategory().getName())
                .imageUrls(salePost.getImages().stream()
                        .map(SalePostImage::getImageUrl)
                        .toList())
                .createdAt(salePost.getCreatedAt())
                .updatedAt(salePost.getUpdatedAt())
                .build();
    }
}
