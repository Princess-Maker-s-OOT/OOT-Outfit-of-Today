package org.example.ootoutfitoftoday.domain.closetimage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.image.entity.Image;

/**
 * [연결 엔티티] ClosetImage: 옷장(Closet)과 이미지(Image)의 1:1 관계를 표현
 * - 목적: 이미지 정보를 별도 엔티티로 분리하여 SRP를 준수하고, 향후 다른 도메인(Clothes 등)과의 일관성을 유지
 * - 제약: 하나의 Closet은 하나의 ClosetImage만 참조하며, 이 엔티티는 하나의 Image만 참조
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "closet_images")
public class ClosetImage extends BaseEntity {

    @Id
    private Long id;

    /**
     * [연관관계] Closet과의 1:1 단방향 관계 (연관관계의 주인)
     * - @JoinColumn(nullable = false) 설정으로, ClosetImage는 Closet에 필수적으로 연결되어야 함
     *
     * @MapsId가 이 필드(id)에 Closet의 ID 값을 매핑
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "closet_id", nullable = false, unique = true) // <--- closet_id FK 매핑
    private Closet closet;

    /**
     * [연관관계] Image와의 1:1 단방향 관계
     * - 이 연결 엔티티는 Image 도메인의 상세 정보 (S3 경로 등)를 참조
     * - @OneToOne: 하나의 ClosetImage는 하나의 Image
     * - nullable = false: 연결 시 Image 정보는 필수
     */
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