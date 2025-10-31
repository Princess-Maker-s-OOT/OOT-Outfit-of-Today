package org.example.ootoutfitoftoday.domain.clothes.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.category.dto.response.QCategoryStat;
import org.example.ootoutfitoftoday.domain.category.entity.QCategory;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesColorCount;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesSizeCount;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.QClothesColorCount;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.QClothesSizeCount;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.entity.QClothes;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesColor;
import org.example.ootoutfitoftoday.domain.clothes.enums.ClothesSize;
import org.example.ootoutfitoftoday.domain.clothesImage.entity.QClothesImage;
import org.example.ootoutfitoftoday.domain.image.entity.QImage;
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

    /**
     * 아래와 같이 동적 조건 메서드로 구현했을 때의 장점
     * 1. null-safe
     * - 각 메서드가 null을 반환하면 조건에서 제외한다.
     * <p>
     * 2. 가독성과 유지보수가 좋다 (코드가 길어지면 길어질 수록 더욱 효과가 좋다.)
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

    /**
     * 사용자의 옷 목록 조회 (필터링 + 무한 스크롤)
     * - 카테고리, 색상, 사이즈로 필터링 가능
     * - 커서 기반 페이징 (무한 스크롤)
     */
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

        // 1단계: 조건에 맞는 Clothes ID만 조회 (페이징 적용)
        List<Long> clothesIds = jpaQueryFactory
                .select(clothes.id)
                .from(clothes)
                .where(
                        isDeletedFalse(),
                        logInUser(userId), // 필수 조건
                        equalsCategory(categoryId),
                        equalsColor(clothesColor),
                        equalsSize(clothesSize),
                        lessThanLastId(lastClothesId)
                )
                .orderBy(clothes.createdAt.desc())
                .limit(size + 1)
                .fetch();

        // 데이터가 없으면 빈 Slice 반환
        if (clothesIds.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), PageRequest.of(0, size), false);
        }

        // 다음 페이지 존재 여부 확인
        boolean hasNext = clothesIds.size() > size;
        if (hasNext) {
            clothesIds.remove(clothesIds.size() - 1); // 초과분 제거
        }

        // 2단계: Fetch Join으로 연관 데이터 한번에 조회
        List<Clothes> result = jpaQueryFactory
                .selectFrom(clothes)
                .distinct()
                .leftJoin(clothes.images, clothesImage).fetchJoin()
                .leftJoin(clothesImage.image, image).fetchJoin()
                .where(
                        clothes.id.in(clothesIds),
                        clothesImage.isDeleted.isNull().or(clothesImage.isDeleted.eq(false))
                )
                .orderBy(clothes.createdAt.desc())
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