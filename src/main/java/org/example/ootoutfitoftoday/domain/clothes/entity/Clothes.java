package org.example.ootoutfitoftoday.domain.clothes.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "clothes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Clothes extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = true) // 추후 유저가 생성된다면 nullable로 변경
//    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ClothesSize clothesSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ClothesColor clothesColor;

    @Column(length = 255, nullable = false)
    private String description;

    @OneToMany(mappedBy = "clothes")
    private List<ClothesImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "clothes")
    private List<ClosetClothesLink> closetClothesLinks = new ArrayList<>();

    @Builder(access = AccessLevel.PROTECTED)
    private Clothes(
            Category category,
//            User user,
            ClothesSize clothesSize,
            ClothesColor clothesColor,
            String description
    ) {
        this.category = category;
//        this.user = user;
        this.clothesSize = clothesSize;
        this.clothesColor = clothesColor;
        this.description = description;
    }

    public static Clothes create(
            Category category,
//            User user,
            ClothesSize clothesSize,
            ClothesColor clothesColor,
            String description
    ) {

        return Clothes.builder()
                .category(category)
//                .user(user)
                .clothesSize(clothesSize)
                .clothesColor(clothesColor)
                .description(description)
                .build();
    }

    public void addImage(ClothesImage image) {
        images.add(image);
        image.updateClothes(this);
    }

    public void removeImage(ClothesImage image) {
        images.remove(image);
        image.updateClothes(null);
    }
}
