package org.example.ootoutfitoftoday.domain.salepost.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.salepost.enums.SaleStatus;
import java.math.BigDecimal;

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
            String title,
            String content,
            BigDecimal price,
            SaleStatus status
    ) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.status = status;
    }

    public static SalePost create(
            String title,
            String content,
            BigDecimal price
    ) {

        return SalePost.builder()
                .title(title)
                .content(content)
                .price(price)
                .status(SaleStatus.SELLING)
                .build();
    }
}
