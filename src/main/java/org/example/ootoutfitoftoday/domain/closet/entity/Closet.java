package org.example.ootoutfitoftoday.domain.closet.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.example.ootoutfitoftoday.domain.closetimage.entity.ClosetImage;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 사용자별 디지털 옷장 정보를 관리하는 엔티티
 * 옷장 정보 외의 이미지는 ClosetImage를 통해 1:1로 관리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "is_deleted = false")
@Table(name = "closets")
public class Closet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255, nullable = true)
    private String description;

    // 공개 여부 (true: 공개, false: 비공개)
    @Column(nullable = false)
    private Boolean isPublic;

    /**
     * [연관관계] User와의 N:1 단방향 관계
     * 옷장은 반드시 한 명의 소유자(User)를 가짐
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * [연관관계] ClosetImage와의 1:1 관계 (연관관계의 주인)
     * - Closet이 ClosetImage를 소유 (DB 테이블의 FK를 Closet 테이블이 가짐)
     * - @JoinColumn(nullable = true): 이미지는 필수가 아니므로, FK 값이 NULL이 허용
     * - CascadeType.ALL: Closet 저장/수정/삭제 시 ClosetImage도 함께 처리
     * - orphanRemoval = true: ClosetImage 연결 해제(null 설정) 시 DB에서 해당 엔티티를 삭제
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "closet_image_id", unique = true, nullable = true)
    private ClosetImage closetImage;

    /**
     * [연관관계] ClosetClothesLink와의 1:N 양방향 관계
     * 옷장에 포함된 의류 목록을 관리
     */
    @OneToMany(mappedBy = "closet")
    private List<ClosetClothesLink> closetClothesLinks = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Closet(
            User user,
            String name,
            String description,
            Boolean isPublic,
            ClosetImage closetImage
    ) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.closetImage = closetImage;
    }

    public static Closet create(
            User user,
            String name,
            String description,
            Boolean isPublic
    ) {

        return Closet.builder()
                .user(user)
                .name(name)
                .description(description)
                .isPublic(isPublic)
                .build();
    }

    public static Closet createWithImage(
            User user,
            String name,
            String description,
            Boolean isPublic,
            Image image
    ) {
        Closet closet = Closet.create(user, name, description, isPublic);
        closet.setClosetImage(image);

        return closet;
    }

    public void setClosetImage(Image image) {
        if (image == null) {
            if (this.closetImage != null) {
                this.closetImage.softDelete();
            }

            return;
        }

        if (this.closetImage == null) {
            this.closetImage = ClosetImage.create(image);
        } else {
            this.closetImage.updateImage(image);
        }
    }

    public void update(
            String name,
            String description,
            Boolean isPublic
    ) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
    }

    // userId를 반환하는 편의 메서드
    public Long getUserId() {

        return this.user.getId();
    }

    // 옷장에 연결된 이미지의 URL을 반환하는 편의 메서드
    public String getImageUrl() {

        return Optional.ofNullable(this.closetImage)
                .map(ClosetImage::getImage)
                .map(Image::getUrl)
                .orElse(null);
    }
}