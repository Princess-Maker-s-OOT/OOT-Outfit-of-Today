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

    @Column(length = 1000, nullable = false)
    private String s3Key;

    @Column(length = 100, nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageType type;

    @Builder(access = AccessLevel.PROTECTED)
    public Image(
            String url,
            String fileName,
            String s3Key,
            String contentType,
            Long size,
            ImageType type
    ) {
        this.url = url;
        this.fileName = fileName;
        this.s3Key = s3Key;
        this.contentType = contentType;
        this.size = size;
        this.type = type;
    }

    // 정적 팩토리 메서드
    public static Image create(
            String url,
            String fileName,
            String s3Key,
            String contentType,
            Long size,
            ImageType type
    ) {
        return Image.builder()
                .url(url)
                .fileName(fileName)
                .s3Key(s3Key)
                .contentType(contentType)
                .size(size)
                .type(type)
                .build();
    }
}