package org.example.ootoutfitoftoday.domain.category.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }

    public static Category create(String name, Category parent) {
        Category category = Category.builder()
                .name(name)
                .parent(parent)
                .build();

        if (parent != null) {
            parent.getChildren().add(category);
        }

        return category;
    }

    public void update(String name, Category newParent) {
        this.name = name;

        if (!Objects.equals(this.parent, newParent)) {

            if (this.parent != null) {
                this.parent.getChildren().remove(this);
            }

            if (newParent != null && !newParent.getChildren().contains(this)) {
                newParent.getChildren().add(this);
            }

            this.parent = newParent;
        }
    }
}