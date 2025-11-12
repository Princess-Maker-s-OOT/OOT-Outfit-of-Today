package org.example.ootoutfitoftoday.domain.wearrecord.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class ClothesWearCount {

    private final Long clothesId; // 옷 중복 방지
    private final String clothesDescription;
    private final Long wearCount; // 착용 횟수 카운트

    @QueryProjection
    public ClothesWearCount(
            Long clothesId,
            String clothesDescription,
            Long wearCount
    ) {
        this.clothesId = clothesId;
        this.clothesDescription = clothesDescription;
        this.wearCount = wearCount;
    }
}
