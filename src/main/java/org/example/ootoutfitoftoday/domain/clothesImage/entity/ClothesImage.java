package org.example.ootoutfitoftoday.domain.clothesImage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.image.entity.Image;

@Entity
@Getter
@Table(name = "clothes_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothesImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 옷 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    private Boolean isMain;

    @Builder(access = AccessLevel.PRIVATE)
    private ClothesImage(
            Clothes clothes,
            Image image,
            Boolean isMain
    ) {
        this.clothes = clothes;
        this.image = image;
        this.isMain = isMain;
    }

    public static ClothesImage create(
            Clothes clothes,
            Image image,
            Boolean isMain
    ) {

        return ClothesImage.builder()
                .clothes(clothes)
                .image(image)
                .isMain(isMain)
                .build();
    }

    public void addClothes(Clothes clothes) {
        this.clothes = clothes;
    }

    public void updateMain(boolean isMain) {
        this.isMain = isMain;
    }
}
