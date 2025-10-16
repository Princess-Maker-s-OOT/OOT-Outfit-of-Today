package org.example.ootoutfitoftoday.domain.salepost.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import org.example.ootoutfitoftoday.domain.user.entity.User;

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

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus status;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "category_id", nullable = false)
//    private Category category;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "clothes_id")
//    private Clothes clothes;
//
//    @OneToMany(mappedBy = "salePost", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<SalePostImage> images = new ArrayList<>();

    @Builder(access = AccessLevel.PROTECTED)
    private SalePost(
            User user,
            Category category,
            Clothes clothes,
            String title,
            String content,
            Long price,
            SaleStatus status
    ) {
        this.user = user;
        this.category = category;
        this.clothes = clothes;
        this.title = title;
        this.content = content;
        this.price = price;
        this.status = SaleStatus.SELLING;
    }

    public static SalePost create(
            User user,
            Category category,
            String title,
            String content,
            Long price
    ) {

        return SalePost.builder()
                .user(user)
                .category(category)
                .clothes(null)
                .title(title)
                .content(content)
                .price(price)
                .status(SaleStatus.SELLING)
                .build();
    }

    public static SalePost create(
            User user,
            Category category,
            Clothes clothes,
            String title,
            String content,
            Long price
    ) {

        return SalePost.builder()
                .user(user)
                .category(category)
                .clothes(clothes)
                .title(title)
                .content(content)
                .price(price)
                .status(SaleStatus.SELLING)
                .build();
    }
}
