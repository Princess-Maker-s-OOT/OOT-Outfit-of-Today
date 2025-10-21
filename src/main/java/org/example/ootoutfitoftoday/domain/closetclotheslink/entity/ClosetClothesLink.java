package org.example.ootoutfitoftoday.domain.closetclotheslink.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "closet_clothes_links")
public class ClosetClothesLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closet_id", nullable = false)
    private Closet closet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Builder(access = AccessLevel.PROTECTED)
    private ClosetClothesLink(
            Closet closet,
            Clothes clothes
    ) {
        this.closet = closet;
        this.clothes = clothes;
        deletedAt = null;
        isDeleted = false;
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

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
