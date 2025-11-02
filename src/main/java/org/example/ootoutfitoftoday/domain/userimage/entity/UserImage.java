package org.example.ootoutfitoftoday.domain.userimage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.image.entity.Image;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_images")
// TODO: 베이스 엔티티 상속 고민
public class UserImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @Builder(access = AccessLevel.PRIVATE)
    private UserImage(Image image) {

        this.image = image;
    }

    public static UserImage create(Image image) {

        return UserImage.builder()
                .image(image)
                .build();
    }

    public void updateImage(Image image) {

        this.image = image;
    }
}
