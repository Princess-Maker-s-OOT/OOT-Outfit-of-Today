package org.example.ootoutfitoftoday.domain.closet.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "closets")
public class Closet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255, nullable = true)
    private String description;

    @Column(length = 500, nullable = true)
    private String imageUrl;

    @Column(nullable = false)
    private boolean visibility = false;

    @OneToMany(mappedBy = "closet")
    private List<ClosetClothesLink> closetClothesLinks = new ArrayList<>();

    @Builder(access = AccessLevel.PROTECTED)
    private Closet(
            String name,
            String description,
            String imageUrl,
            Boolean visibility
    ) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.visibility = visibility;
    }

    public static Closet create(
            String name,
            String description,
            String imageUrl,
            Boolean visibility
    ) {
        return Closet.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .visibility(visibility)
                .build();
    }
}
