package org.example.ootoutfitoftoday.domain.clothes.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "clothes_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothesImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    @Column(length = 500, nullable = true)
    private String imageUrl;

    @Builder(access = AccessLevel.PROTECTED)
    private ClothesImage(
            Clothes clothes,
            String imageUrl
    ) {
        this.clothes = clothes;
        this.imageUrl = imageUrl;
    }

    public static ClothesImage create(
            Clothes clothes,
            String imageUrl
    ) {

        return ClothesImage.builder()
                .clothes(clothes)
                .imageUrl(imageUrl)
                .build();
    }

    public void updateClothes(Clothes clothes) {
        this.clothes = clothes;
    }
}
