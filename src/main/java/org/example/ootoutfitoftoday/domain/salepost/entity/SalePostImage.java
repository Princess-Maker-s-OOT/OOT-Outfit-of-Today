package org.example.ootoutfitoftoday.domain.salepost.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sale_post_images")
public class SalePostImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Integer displayOrder; // 이미지 표시 순서 (조회 시 정렬 기준)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_post_id", nullable = false)
    private SalePost salePost;

    @Builder(access = AccessLevel.PROTECTED)
    private SalePostImage(
            String imageUrl,
            Integer displayOrder
    ) {
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    public static SalePostImage create(
            String imageUrl,
            Integer displayOrder
    ) {
        validateImageUrl(imageUrl);

        return SalePostImage.builder()
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .build();
    }

    private static void  validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new SalePostException(SalePostErrorCode.EMPTY_IMAGE_URL);
        }
    }

    public void setSalePost(SalePost salePost) {
        this.salePost = salePost;
    }
}
