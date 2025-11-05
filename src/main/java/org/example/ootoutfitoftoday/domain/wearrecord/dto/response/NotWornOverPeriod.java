package org.example.ootoutfitoftoday.domain.wearrecord.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotWornOverPeriod {

    private final Long clothesId;
    private final String clothesDescription;
    private final LocalDateTime lastWornAt; // 마지막 해당 옷 착용 시각
    private final Long daysNotWorn; // 옷 미착용 기간

    // QueryDSL에서 사용할 생성자
    @QueryProjection
    public NotWornOverPeriod(Long clothesId, String clothesDescription, LocalDateTime lastWornAt) {
        this.clothesId = clothesId;
        this.clothesDescription = clothesDescription;
        this.lastWornAt = lastWornAt;
        this.daysNotWorn = null;
    }

    // 서비스에서 사용할 생성자
    @Builder
    public NotWornOverPeriod(Long clothesId, String clothesDescription, LocalDateTime lastWornAt, Long daysNotWorn) {
        this.clothesId = clothesId;
        this.clothesDescription = clothesDescription;
        this.lastWornAt = lastWornAt;
        this.daysNotWorn = daysNotWorn;
    }
}
