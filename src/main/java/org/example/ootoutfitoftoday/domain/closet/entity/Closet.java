package org.example.ootoutfitoftoday.domain.closet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "closets")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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
    private Boolean visibility = false;

//    @OneToMany(mappedBy = "closet")
//    private List<ClosetClothesLink> closetClothesLinks = new ArrayList<>();

    @Builder
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
