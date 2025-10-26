package org.example.ootoutfitoftoday.domain.clothes.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.category.dto.response.QCategoryStat;
import org.example.ootoutfitoftoday.domain.category.entity.QCategory;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.*;
import org.example.ootoutfitoftoday.domain.clothes.entity.QClothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@RequiredArgsConstructor
public class ClothesCustomRepositoryImpl implements ClothesCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QClothes clothes = QClothes.clothes;
    private final QCategory category = QCategory.category;

    /**
     *  아래와 같이 동적 조건 메서드로 구현했을 때의 장점
     *  1. null-safe
     *  - 각 메서드가 null을 반환하면 조건에서 제외한다.
     *
     *  2. 가독성과 유지보수가 좋다 (코드가 길어지면 길어질 수록 더욱 효과가 좋다.)
     */
    // 항상 삭제되지 않은 데이터만 조회하도록 구현
    private BooleanExpression isDeletedFalse() {

        return clothes.isDeleted.eq(false);
    }

    // 유저가 null 이면 조건에서 제외
    private BooleanExpression logInUser(Long userId) {

        return userId != null ? clothes.user.id.eq(userId) : null;
    }

    // 카테고리 값이 null 이면 조건에서 제외
    private BooleanExpression equalsCategory(Long categoryId) {

        return categoryId != null ? clothes.category.id.eq(categoryId) : null;
    }

    // 색상 값이 null 이면 조건에서 제외
    private BooleanExpression equalsColor(ClothesColor clothesColor) {

        return clothesColor != null ? clothes.clothesColor.eq(clothesColor) : null;
    }

    // 사이즈 값이 null 이면 조건에서 제외
    private BooleanExpression equalsSize(ClothesSize clothesSize) {

        return clothesSize != null ? clothes.clothesSize.eq(clothesSize) : null;
    }

    // 마지막 아이디가 null 이면 첫페이지 조회
    private BooleanExpression lessThanLastId(Long lastId) {

        return lastId != null ? clothes.id.lt(lastId) : null;
    }

    @Override
    public Slice<ClothesResponse> findAllByIsDeletedFalse(
            Long categoryId,
            Long userId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            Long lastClothesId, // 커서 기준 (무한스크롤용)
            int size
    ) {

        List<ClothesResponse> result = jpaQueryFactory
                .select(new QClothesResponse(
                        clothes.id,
                        clothes.category.id,
                        clothes.user.id,
                        clothes.clothesSize,
                        clothes.clothesColor,
                        clothes.description
                ))
                .from(clothes)
                .where(
                        isDeletedFalse(),
                        logInUser(userId),
                        equalsCategory(categoryId),
                        equalsColor(clothesColor),
                        equalsSize(clothesSize),
                        lessThanLastId(lastClothesId)
                )
                .orderBy(clothes.createdAt.desc())
                .limit(size + 1) // 요청된 size 보다 하나 더 가져와서 다음 페이지가 있는 지 없는 지 판단할 때 사용, 참고! limit는 입력 값보다 남아 있는 데이터가 적다면 남은 만큼만 가져옴
                .fetch();

        boolean hasNext = result.size() > size; // (size + 1)번째 데이터가 있다면 true

        if (hasNext) {
            result.remove(size); // 다음 페이지 존재만 확인 후 초과분 +1은 다시 삭제
        }

        return new SliceImpl<>(result, PageRequest.of(0, size), hasNext);
    }

    @Override
    public List<CategoryStat> countTopCategoryStats() {

        QCategory parent = new QCategory("parent");
        QCategory grandParent = new QCategory("grandParent");

        var rootName = grandParent.name
                .coalesce(parent.name)
                .coalesce(category.name);

        return jpaQueryFactory
                .select(new QCategoryStat(
                        rootName,
                        clothes.count()
                ))
                .from(clothes)
                .join(clothes.category, category)
                .leftJoin(category.parent, parent)
                .leftJoin(parent.parent, grandParent)
                .where(isDeletedFalse())
                .groupBy(rootName)
                .orderBy(clothes.count().desc())
                .fetch();
    }

    @Override
    public List<ClothesColorCount> clothesColorsCount() {

        return jpaQueryFactory
                .select(new QClothesColorCount(
                        clothes.clothesColor,
                        clothes.count()
                ))
                .from(clothes)
                .where(isDeletedFalse())
                .groupBy(clothes.clothesColor)
                .orderBy(clothes.count().desc())
                .fetch();
    }

    @Override
    public List<ClothesSizeCount> clothesSizesCount() {

        return jpaQueryFactory
                .select(new QClothesSizeCount(
                        clothes.clothesSize,
                        clothes.count()
                ))
                .from(clothes)
                .where(isDeletedFalse())
                .groupBy(clothes.clothesSize)
                .orderBy(clothes.count().desc())
                .fetch();
    }

    @Override
    public List<CategoryStat> findTopCategoryStats() {

        return jpaQueryFactory
                .select(new QCategoryStat(
                        category.name,
                        clothes.count()
                ))
                .from(clothes)
                .join(clothes.category, category)
                .where(isDeletedFalse())
                .groupBy(category.id, category.name)
                .orderBy(clothes.count().desc())
                .limit(10)
                .fetch();
    }
}

