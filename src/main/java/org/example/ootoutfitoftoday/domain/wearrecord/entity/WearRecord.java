package org.example.ootoutfitoftoday.domain.wearrecord.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "wear_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WearRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    @Column(name = "worn_at", nullable = false)
    private LocalDateTime wornAt;

    @Builder(access = AccessLevel.PRIVATE)
    private WearRecord(
            User user,
            Clothes clothes,
            LocalDateTime wornAt
    ) {
        this.user = user;
        this.clothes = clothes;
        this.wornAt = wornAt;
    }

    public static WearRecord create(
            User user,
            Clothes clothes,
            LocalDateTime wornAt
    ) {

        return WearRecord.builder()
                .user(user)
                .clothes(clothes)
                .wornAt(wornAt)
                .build();
    }
}