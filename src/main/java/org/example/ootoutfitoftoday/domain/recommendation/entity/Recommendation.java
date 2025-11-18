package org.example.ootoutfitoftoday.domain.recommendation.entity;

import com.ootcommon.recommendation.status.RecommendationStatus;
import com.ootcommon.recommendation.type.RecommendationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.user.entity.User;

@Entity
@Getter
@Table(name = "recommendations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommendation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationType type;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private Recommendation(
            User user,
            Clothes clothes,
            RecommendationType type,
            String reason,
            RecommendationStatus status
    ) {
        this.user = user;
        this.clothes = clothes;
        this.type = type;
        this.reason = reason;
        this.status = status;
    }

    public static Recommendation createForUnwornClothes(
            User user,
            Clothes clothes,
            RecommendationType type,
            String reason
    ) {

        return Recommendation.builder()
                .user(user)
                .clothes(clothes)
                .type(type)
                .reason(reason)
                .status(RecommendationStatus.PENDING)

                .build();
    }

    public void accept() {
        this.status = RecommendationStatus.ACCEPTED;
    }

    public void reject() {
        this.status = RecommendationStatus.REJECTED;
    }
}