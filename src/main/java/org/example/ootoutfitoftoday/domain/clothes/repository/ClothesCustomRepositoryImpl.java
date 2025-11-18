package org.example.ootoutfitoftoday.domain.clothes.repository;

import com.ootcommon.category.response.CategoryStat;
import com.ootcommon.category.response.QCategoryStat;
import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import com.ootcommon.clothes.response.ClothesColorCount;
import com.ootcommon.clothes.response.ClothesSizeCount;
import com.ootcommon.clothes.response.QClothesColorCount;
import com.ootcommon.clothes.response.QClothesSizeCount;
import com.ootcommon.wearrecord.response.ClothesWearCount;
import com.ootcommon.wearrecord.response.NotWornOverPeriod;
import com.ootcommon.wearrecord.response.QClothesWearCount;
import com.ootcommon.wearrecord.response.QNotWornOverPeriod;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.entity.QCategory;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.entity.QClothes;
import org.example.ootoutfitoftoday.domain.clothesImage.entity.QClothesImage;
import org.example.ootoutfitoftoday.domain.image.entity.QImage;
import org.example.ootoutfitoftoday.domain.wearrecord.entity.QWearRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ClothesCustomRepositoryImpl implements ClothesCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QClothes clothes = QClothes.clothes;
    private final QCategory category = QCategory.category;
    private final QWearRecord wearRecord = QWearRecord.wearRecord;

    private BooleanExpression isDeletedFalse() {

        return clothes.isDeleted.eq(false);
    }

    private BooleanExpression logInUser(Long userId) {

        return userId != null ? clothes.user.id.eq(userId) : null;
    }

    private BooleanExpression equalsCategory(Long categoryId) {

        return categoryId != null ? clothes.category.id.eq(categoryId) : null;
    }

    private BooleanExpression equalsColor(ClothesColor clothesColor) {

        return clothesColor != null ? clothes.clothesColor.eq(clothesColor) : null;
    }

    private BooleanExpression equalsSize(ClothesSize clothesSize) {

        return clothesSize != null ? clothes.clothesSize.eq(clothesSize) : null;
    }

    private BooleanExpression lessThanLastId(Long lastId) {

        return lastId != null ? clothes.id.lt(lastId) : null;
    }

    @Override
    public Slice<Clothes> findAllByIsDeletedFalse(
            Long userId,
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            Long lastClothesId,
            int size
    ) {
        QClothesImage clothesImage = QClothesImage.clothesImage;
        QImage image = QImage.image;

        List<Long> clothesIds = jpaQueryFactory
                .select(clothes.id)
                .from(clothes)
                .where(
                        isDeletedFalse(),
                        logInUser(userId),
                        equalsCategory(categoryId),
                        equalsColor(clothesColor),
                        equalsSize(clothesSize),
                        lessThanLastId(lastClothesId)
                )
                .orderBy(clothes.createdAt.desc(), clothes.id.asc())
                .limit(size + 1)
                .fetch();

        if (clothesIds.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), PageRequest.of(0, size), false);
        }

        boolean hasNext = clothesIds.size() > size;
        if (hasNext) {
            clothesIds.remove(clothesIds.size() - 1);
        }

        List<Clothes> result = jpaQueryFactory
                .selectFrom(clothes)
                .distinct()
                .leftJoin(clothes.images, clothesImage).fetchJoin()
                .leftJoin(clothesImage.image, image).fetchJoin()
                .where(
                        clothes.id.in(clothesIds),
                        clothesImage.isDeleted.isNull().or(clothesImage.isDeleted.eq(false))
                )
                .orderBy(clothes.createdAt.desc(), clothes.id.asc())
                .fetch();

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
                .orderBy(clothes.count().desc(), rootName.asc())
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
                .orderBy(clothes.count().desc(), clothes.clothesColor.asc())
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
                .orderBy(clothes.count().desc(), clothes.clothesSize.asc())
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
                .orderBy(clothes.count().desc(), category.id.asc())
                .limit(10)
                .fetch();
    }

    @Override
    public List<ClothesWearCount> leastWornClothes(Long userId) {

        return jpaQueryFactory
                .select(new QClothesWearCount(
                        clothes.id,
                        clothes.description,
                        wearRecord.id.count()
                ))
                .from(clothes)
                .leftJoin(wearRecord)
                .on(wearRecord.clothes.id.eq(clothes.id)
                        .and(wearRecord.user.id.eq(userId)))
                .where(clothes.user.id.eq(userId))
                .groupBy(clothes.id, clothes.description)
                .orderBy(wearRecord.id.count().asc(), clothes.id.asc())
                .limit(5)
                .fetch();
    }

    @Override
    public List<NotWornOverPeriod> notWornOverPeriod(Long userId) {

        return jpaQueryFactory
                .select(new QNotWornOverPeriod(
                        clothes.id,
                        clothes.description,
                        clothes.lastWornAt
                ))
                .from(clothes)
                .leftJoin(wearRecord)
                .on(wearRecord.clothes.id.eq(clothes.id)
                        .and(wearRecord.user.id.eq(userId)))
                .where(clothes.user.id.eq(userId))
                .groupBy(clothes.id, clothes.description)
                .orderBy(clothes.lastWornAt.asc().nullsFirst(), clothes.id.asc())
                .limit(10)
                .fetch();
    }
}