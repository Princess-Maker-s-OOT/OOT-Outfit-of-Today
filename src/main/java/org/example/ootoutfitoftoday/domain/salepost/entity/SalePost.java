package org.example.ootoutfitoftoday.domain.salepost.entity;

import com.ootcommon.salepost.enums.SaleStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
    @Where(clause = "is_deleted = false")
    private List<SalePostImage> images = new ArrayList<>();

    /**
     * [연관관계] Recommendation과의 N:1 단방향 관계
     * - nullable: 추천 없이 직접 작성된 판매글도 존재 가능
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id")
    private Recommendation recommendation;

    @Builder(access = AccessLevel.PROTECTED)
    private SalePost(
            User user,
            Category category,
            String title,
            String content,
            BigDecimal price,
            SaleStatus status,
            String tradeAddress,
            String tradeLocation,
            Recommendation recommendation
    ) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.price = price;
        this.status = status;
        this.tradeAddress = tradeAddress;
        this.tradeLocation = tradeLocation;
        this.recommendation = recommendation;
    }

    public static SalePost create(
            User user,
            Category category,
            String title,
            String content,
            BigDecimal price,
            String tradeAddress,
            String tradeLocation,
            List<Image> images
    ) {
        validatePrice(price);
        validateImages(images);

        SalePost salePost = SalePost.builder()
                .user(user)
                .category(category)
                .title(title)
                .content(content)
                .price(price)
                .status(SaleStatus.AVAILABLE)
                .tradeAddress(tradeAddress)
                .tradeLocation(tradeLocation)
                .build();

        for (int i = 0; i < images.size(); i++) {
            boolean isMain = (i == 0);
            SalePostImage salePostImage = SalePostImage.create(
                    images.get(i),
                    i + 1,
                    isMain
            );
            salePost.addImage(salePostImage);
        }

        return salePost;
    }

    public static SalePost createFromRecommendation(
            Recommendation recommendation,
            Category category,
            String title,
            String content,
            BigDecimal price,
            String tradeAddress,
            String tradeLocation,
            List<Image> images
    ) {
        validatePrice(price);
        validateImages(images);

        SalePost salePost = SalePost.builder()
                .user(recommendation.getUser())
                .category(category)
                .title(title)
                .content(content)
                .price(price)
                .status(SaleStatus.AVAILABLE)
                .tradeAddress(tradeAddress)
                .tradeLocation(tradeLocation)
                .recommendation(recommendation)
                .build();

        for (int i = 0; i < images.size(); i++) {
            boolean isMain = (i == 0);
            SalePostImage salePostImage = SalePostImage.create(
                    images.get(i),
                    i + 1,
                    isMain
            );
            salePost.addImage(salePostImage);
        }

        return salePost;
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new SalePostException(SalePostErrorCode.INVALID_PRICE);
        }
    }

    private static void validateImages(List<Image> images) {
        if (images == null || images.isEmpty()) {
            throw new SalePostException(SalePostErrorCode.EMPTY_IMAGES);
        }

        Set<Long> uniqueIds = images.stream()
                .map(Image::getId)
                .collect(Collectors.toSet());

        if (uniqueIds.size() != images.size()) {
            throw new SalePostException(SalePostErrorCode.DUPLICATE_IMAGE);
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
            List<Image> images
    ) {
        validatePrice(price);
        validateImages(images);

        this.category = category;
        this.title = title;
        this.content = content;
        this.price = price;
        this.tradeAddress = tradeAddress;
        this.tradeLocation = tradeLocation;

        updateImages(images);
    }

    public void updateImages(List<Image> images) {

        validateImages(images);

        this.images.clear();

        for (int i = 0; i < images.size(); i++) {
            boolean isMain = (i == 0);
            SalePostImage salePostImage = SalePostImage.create(
                    images.get(i),
                    i + 1,
                    isMain
            );
            this.addImage(salePostImage);
        }
    }

    public boolean isOwnedBy(Long userId) {

        return this.user != null && Objects.equals(this.user.getId(), userId);
    }

    public void updateStatus(SaleStatus newStatus) {
        this.status = newStatus;
    }

    public User getSeller() {

        return user;
    }
}