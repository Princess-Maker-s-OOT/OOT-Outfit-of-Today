package org.example.ootoutfitoftoday.domain.closetimage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.image.entity.Image;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "closet_images")
public class ClosetImage extends BaseEntity {

    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "closet_id", nullable = false, unique = true)
    private Closet closet;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false, unique = true)
    private Image image;

    @Builder(access = AccessLevel.PROTECTED)
    private ClosetImage(Image image, Closet closet) {
        this.image = image;
        this.closet = closet;
    }

    public static ClosetImage create(Image image, Closet closet) {
        return ClosetImage.builder()
                .image(image)
                .closet(closet)
                .build();
    }

    public void updateImage(Image newImage) {
        this.image = newImage;
    }
}