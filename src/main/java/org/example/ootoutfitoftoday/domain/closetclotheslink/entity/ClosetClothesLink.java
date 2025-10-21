package org.example.ootoutfitoftoday.domain.closetclotheslink.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "closet_clothes_links")
public class ClosetClothesLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closet_id", nullable = false)
    private Closet closet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    @Builder(access = AccessLevel.PROTECTED)
    private ClosetClothesLink(
            Closet closet,
            Clothes clothes
    ) {
        this.closet = closet;
        this.clothes = clothes;
    }

    public static ClosetClothesLink create(
            Closet closet,
            Clothes clothes
    ) {

        return ClosetClothesLink.builder()
                .closet(closet)
                .clothes(clothes)
                .build();
    }
}
