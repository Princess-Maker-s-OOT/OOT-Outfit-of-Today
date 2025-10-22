package org.example.ootoutfitoftoday.domain.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "images")
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String url;

    @Column(length = 255, nullable = false)
    private String fileName;

    @Column(length = 100, nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType type;

    @Builder(access = AccessLevel.PROTECTED)
    public Image(String url, String fileName, String contentType, Long size, ImageType type) {
        this.url = url;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.type = type;
    }
}
