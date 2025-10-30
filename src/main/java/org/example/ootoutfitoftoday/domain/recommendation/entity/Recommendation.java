package org.example.ootoutfitoftoday.domain.recommendation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ootoutfitoftoday.common.entity.BaseEntity;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.recommendation.status.RecommendationStatus;
import org.example.ootoutfitoftoday.domain.recommendation.type.RecommendationType;
import org.example.ootoutfitoftoday.domain.user.entity.User;

@Entity
@Getter
@Table(name = "recommendations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommendation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * [연관관계] Clothes와의 N:1 단방향 관계
     * - 여러 개의 Recommendation은 하나의 Clothes를 참조
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    /**
     * [연관관계] User와의 N:1 단방향 관계
     * - 여러 개의 Recommendation은 한 명의 User에게 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 추천의 종류 (기부 또는 판매)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationType type;

    // 추천이 발생한 이유 (예: '마지막 착용일 1년 초과')
    @Column(nullable = false)
    private String reason;

    // 추천 기록의 현재 상태 관리
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

    // 생성 시 상태는 항상 PENDING으로 초기화
    public static Recommendation ofUnwornClothes(
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