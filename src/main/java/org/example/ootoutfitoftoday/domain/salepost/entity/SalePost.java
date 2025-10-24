package org.example.ootoutfitoftoday.domain.salepost.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sale_posts")
public class SalePost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStatus status;

    @Column(nullable = false, length = 50)
    private String tradeAddress;

    @Column(nullable = false, columnDefinition = "POINT SRID 4326", updatable = false, insertable = false)
    private String tradeLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "salePost", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @BatchSize(size = 100)
    private List<SalePostImage> images = new ArrayList<>();

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "clothes_id")
//    private Clothes clothes;

    @Builder(access = AccessLevel.PROTECTED)
    private SalePost(
            User user,
            Category category,
            String title,
            String content,
            BigDecimal price,
            SaleStatus status,
            String tradeAddress,
            String tradeLocation
    ) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.price = price;
        this.status = status;
        this.tradeAddress = tradeAddress;
        this.tradeLocation = tradeLocation;
    }

    public static SalePost create(
            User user,
            Category category,
            String title,
            String content,
            BigDecimal price,
            String tradeAddress,
            String tradeLocation,
            List<String> imageUrls
    ) {
        validatePrice(price);
        validateImages(imageUrls);

        SalePost salePost = SalePost.builder()
                .user(user)
                .category(category)
                .title(title)
                .content(content)
                .price(price)
                .status(SaleStatus.SELLING)
                .tradeAddress(tradeAddress)
                .tradeLocation(tradeLocation)
                .build();

        // 이미지 URL 리스트를 순서대로 SalePostImage 엔티티로 변환 (displayOrder: 1, 2, 3, ...)
        for (int i = 0; i < imageUrls.size(); i++) {
            SalePostImage image = SalePostImage.create(imageUrls.get(i), i + 1);
            salePost.addImage(image);
        }

        return salePost;
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SalePostException(SalePostErrorCode.INVALID_PRICE);
        }
    }

    private static void validateImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new SalePostException(SalePostErrorCode.EMPTY_IMAGES);
        }

        Set<String> uniqueUrls = new HashSet<>(imageUrls);
        if (uniqueUrls.size() != imageUrls.size()) {
            throw new SalePostException(SalePostErrorCode.DUPLICATE_IMAGE_URL);
        }
    }

    public void addImage(SalePostImage image) {
        this.images.add(image);
        image.setSalePost(this);
    }

    public void update(
            Category category,
            String title,
            String content,
            BigDecimal price,
            String tradeAddress,
            String tradeLocation,
            List<String> imageUrls
    ) {
        validatePrice(price);
        validateImages(imageUrls);

        this.category = category;
        this.title = title;
        this.content = content;
        this.price = price;
        this.tradeAddress = tradeAddress;
        this.tradeLocation = tradeLocation;

        updateImages(imageUrls);
    }

    private void updateImages(List<String> imageUrls) {

        this.images.clear();

        for (int i = 0; i < imageUrls.size(); i++) {
            SalePostImage image = SalePostImage.create(imageUrls.get(i), i + 1);
            this.addImage(image);
        }
    }

    public boolean isOwnedBy(Long userId) {

        return this.user != null && Objects.equals(this.user.getId(), userId);
    }

    public void updateStatus(SaleStatus newStatus) {
        this.status = newStatus;
    }
}
