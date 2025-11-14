package org.example.ootoutfitoftoday.domain.wearrecord.repository;

import com.ootcommon.wearrecord.response.ClothesWearCount;
import com.ootcommon.wearrecord.response.QClothesWearCount;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.clothes.entity.QClothes;
import org.example.ootoutfitoftoday.domain.wearrecord.entity.QWearRecord;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RequiredArgsConstructor
public class WearCustomRepositoryImpl implements WearCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QWearRecord wearRecord = QWearRecord.wearRecord;
    private final QClothes clothes = QClothes.clothes;

    // 이번 주 착용 빈도 높은 옷
    @Override
    public List<ClothesWearCount> wornThisWeek(Long userId, LocalDate baseDate) {

        // targetDate가 null이면 오늘 기준
        if (baseDate == null) {
            baseDate = LocalDate.now();
        }

        // 주차의 시작(월요일 0시 0분 0초)
        LocalDateTime startOfWeek = baseDate
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        // 주차의 끝(일요일 23:59:59.999999999)
        LocalDateTime endOfWeek = baseDate
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(LocalTime.MAX);

        return jpaQueryFactory
                .select(new QClothesWearCount(
                        wearRecord.clothes.id,
                        wearRecord.clothes.description,
                        wearRecord.id.count()
                ))
                .from(wearRecord)
                .leftJoin(wearRecord.clothes, clothes)
                .where(
                        wearRecord.user.id.eq(userId),
                        wearRecord.wornAt.between(startOfWeek, endOfWeek)
                )
                .groupBy(wearRecord.clothes.id, wearRecord.clothes.description)
                .orderBy(wearRecord.id.count().desc(), wearRecord.clothes.id.count().asc())
                .limit(10)
                .fetch();
    }

    // 자주 입은 옷 (전체 기간)
    @Override
    public List<ClothesWearCount> topWornClothes(Long userId) {

        return jpaQueryFactory
                .select(new QClothesWearCount(
                        wearRecord.clothes.id,
                        wearRecord.clothes.description,
                        wearRecord.id.count()
                ))
                .from(wearRecord)
                .leftJoin(wearRecord.clothes, clothes)
                .where(wearRecord.user.id.eq(userId))
                .groupBy(wearRecord.clothes.id, wearRecord.clothes.description)
                .orderBy(wearRecord.id.count().desc(), wearRecord.clothes.id.count().asc())
                .limit(5)
                .fetch();
    }
}
