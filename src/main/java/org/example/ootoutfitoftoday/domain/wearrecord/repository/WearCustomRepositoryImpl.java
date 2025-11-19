package org.example.ootoutfitoftoday.domain.wearrecord.repository;

import com.ootcommon.wearrecord.response.ClothesWearCount;
import com.ootcommon.wearrecord.response.QClothesWearCount;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.clothes.entity.QClothes;
import org.example.ootoutfitoftoday.domain.wearrecord.entity.QWearRecord;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class WearCustomRepositoryImpl implements WearCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QWearRecord wearRecord = QWearRecord.wearRecord;
    private final QClothes clothes = QClothes.clothes;

    @Override
    public List<ClothesWearCount> wornThisWeek(Long userId, LocalDate baseDate) {
        log.debug("이번 주 착용 빈도 쿼리 실행 시작 - 사용자 ID: {}, 기준 날짜: {}", userId, baseDate);

        if (baseDate == null) {
            baseDate = LocalDate.now();
            log.debug("기준 날짜가 null이므로 오늘 날짜 사용: {}", baseDate);
        }

        LocalDateTime startOfWeek = baseDate
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        LocalDateTime endOfWeek = baseDate
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(LocalTime.MAX);

        log.debug("주간 범위 설정 - 시작: {}, 종료: {}", startOfWeek, endOfWeek);

        List<ClothesWearCount> result = jpaQueryFactory
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

        log.debug("이번 주 착용 빈도 쿼리 완료 - 결과 수: {}", result.size());

        return result;
    }

    @Override
    public List<ClothesWearCount> topWornClothes(Long userId) {
        log.debug("자주 입은 옷 쿼리 실행 시작 - 사용자 ID: {}", userId);

        List<ClothesWearCount> result = jpaQueryFactory
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

        log.debug("자주 입은 옷 쿼리 완료 - 결과 수: {}", result.size());

        return result;
    }
}