package org.example.ootoutfitoftoday.domain.clothes.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.entity.QClothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class CustomClothesRepositoryImpl implements CustomClothesRepository {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 옷 리스트 조회 쿼리
     * @param categoryId 카테코리 아이디의 값을 파람으로 받아 필터링
     * @param clothesColor 색상의 값을 파람으로 받아 필터링
     * @param clothesSize 사이즈의 값을 파람으로 받아 필터링
     * @param pageable 페이지의 값을 파람으로 받아 필터링
     * - 위의 파람 값이 null 이라면 필터링에서 제외
     * Todo: 유저의 값이 들어온다면 유저는 필수 조건으로 필터링할 예정
     *     + 엔티티가 아닌 리스폰스로 바로 반환하는 것도 방법인데 그것은 추후에 리팩토링으로 생각하겠음.
     *     + 카테고리 아이디가 존재하지 않을 때도 일단 조회가 되는 상황(빈 리스트) 이것도 추후에 리팩토링으로 생각.
     */
    @Override
    public Page<Clothes> findAllByIsDeletedFalse(
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            Pageable pageable
    ) {
        // QueryDSL 에서 사용되는 객체 생성
        QClothes clothes = QClothes.clothes;

        // BooleanBuilder를 사용하여 동적으로 필터링
        BooleanBuilder builder = new BooleanBuilder();
        // 삭제된 데이터는 출력되지 않도록 필수 조건으로 세팅
        builder.and(clothes.isDeleted.eq(false));

        // 아래의 조건문으로 null 이라면 건너뛰고 null이 아닐 경우 조건으로 체크
        if (categoryId != null) {
            builder.and(clothes.category.id.eq(categoryId));
        }

        if (clothesColor != null) {
            builder.and(clothes.clothesColor.eq(clothesColor));
        }

        if (clothesSize != null) {
            builder.and(clothes.clothesSize.eq(clothesSize));
        }

        // 실제 데이터 조회
        List<Clothes> result = jpaQueryFactory
                .selectFrom(clothes)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(clothes.updatedAt.desc())
                .fetch();

        // 데이터 개수 조회
        Long total = jpaQueryFactory
                .select(clothes.count())
                .from(clothes)
                .where(builder)
                .fetchOne();

        // null-safe
        long totalCount = total != null ? total : 0L;

        return new PageImpl<>(
                result,
                pageable,
                totalCount
        );
    }
}

