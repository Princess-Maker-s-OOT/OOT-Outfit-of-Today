package org.example.ootoutfitoftoday.domain.closet.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.example.ootoutfitoftoday.domain.user.entity.User;

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

    // 공개 여부 (true: 공개, false: 비공개)
    @Column(nullable = false)
    private Boolean isPublic;

    // 옷장의 소유자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 옷장과 의류 간의 중간 테이블 역할을 하는 엔티티
    @OneToMany(mappedBy = "closet")
    private List<ClosetClothesLink> closetClothesLinks = new ArrayList<>();

    @Builder(access = AccessLevel.PROTECTED)
    private Closet(
            User user,
            String name,
            String description,
            String imageUrl,
            Boolean isPublic
    ) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isPublic = isPublic;
    }

    public static Closet create(
            User user,
            String name,
            String description,
            String imageUrl,
            Boolean isPublic
    ) {

        return Closet.builder()
                .user(user)
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .isPublic(isPublic)
                .build();
    }

    // userId를 반환하는 편의 메서드
    public Long getUserId() {
        
        return this.user.getId();
    }
}