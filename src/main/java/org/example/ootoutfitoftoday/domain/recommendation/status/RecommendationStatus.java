package org.example.ootoutfitoftoday.domain.recommendation.status;

public enum RecommendationStatus {
    // 추천 기록이 생성되었으나 아직 사용자가 응답하지 않은 상태
    PENDING,

    // 사용자가 추천(기부/판매)을 수락하고 진행하기로 한 상태
    ACCEPTED,

    // 사용자가 해당 추천을 무시하거나 거절한 상태
    REJECTED
}