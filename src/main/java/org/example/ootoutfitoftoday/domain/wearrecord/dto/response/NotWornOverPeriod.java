package org.example.ootoutfitoftoday.domain.wearrecord.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
public class NotWornOverPeriod {

    private final Long clothesId;
    private final String clothesDescription;
    private final LocalDateTime lastWornAt; // 마지막 해당 옷 착용 시각
    private final Long daysNotWorn; // 옷 미착용 기간

    @QueryProjection
    public NotWornOverPeriod(Long clothesId, String clothesDescription, LocalDateTime lastWornAt) {
        this.clothesId = clothesId;
        this.clothesDescription = clothesDescription;
        this.lastWornAt = lastWornAt;

        // daysNotWorn 계산 로직:
        if (lastWornAt == null) {
            this.daysNotWorn = 0L;
        } else {
            // 오늘 날짜와 마지막 착용일 간의 일(Day) 차이를 계산
            this.daysNotWorn = ChronoUnit.DAYS.between(lastWornAt, LocalDateTime.now());
        }
    }
}
