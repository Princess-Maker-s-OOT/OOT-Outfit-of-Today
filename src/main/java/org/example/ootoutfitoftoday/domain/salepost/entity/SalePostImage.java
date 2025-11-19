package org.example.ootoutfitoftoday.domain.salepost.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
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
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean isMain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_post_id", nullable = false)
    private SalePost salePost;

    @Builder(access = AccessLevel.PROTECTED)
    private SalePostImage(
            String imageUrl,
            Integer displayOrder,
            Boolean isMain
    ) {
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.isMain = isMain;
    }

    public static SalePostImage create(
            Image image,
            Integer displayOrder,
            Boolean isMain
    ) {
        validateImage(image);

        return SalePostImage.builder()
                .imageUrl(image.getUrl())
                .displayOrder(displayOrder)
                .isMain(isMain)
                .build();
    }

    private static void validateImage(Image image) {
        if (image == null) {
            throw new SalePostException(SalePostErrorCode.EMPTY_IMAGES);
        }
    }

    public void setSalePost(SalePost salePost) {
        this.salePost = salePost;
    }

    public void updateMain(boolean isMain) {
        this.isMain = isMain;
    }
}