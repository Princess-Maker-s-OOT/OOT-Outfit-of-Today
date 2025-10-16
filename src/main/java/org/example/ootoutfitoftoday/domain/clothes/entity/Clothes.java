package org.example.ootoutfitoftoday.domain.clothes.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.example.ootoutfitoftoday.domain.clothes.enums.Color;
import org.example.ootoutfitoftoday.domain.clothes.enums.Size;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "clothes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Clothes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Color color;

    @Column(length = 255, nullable = false)
    private String description;

    @OneToMany(mappedBy = "clothes")
    private List<ClothesImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "clothes")
    private List<ClosetClothesLink> closetClothesLinks = new ArrayList<>();

    @Builder(access = AccessLevel.PROTECTED)
    private Clothes(
            Category category,
            User user,
            Size size,
            Color color,
            String description
    ) {
        this.category = category;
        this.user = user;
        this.size = size;
        this.color = color;
        this.description = description;
    }

    public static Clothes create(
            Category category,
            User user,
            Size size,
            Color color,
            String description
    ) {

        return Clothes.builder()
                .category(category)
                .user(user)
                .size(size)
                .color(color)
                .description(description)
                .build();
    }

    public void addImage(ClothesImage image) {
        images.add(image);
        image.updateClothes(this);
    }

    public void removeImage(ClothesImage image) {
        images.remove(image);
        image.updateClothes(null);
    }
}
