package org.example.ootoutfitoftoday.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.closet.entity.QCloset;
import org.example.ootoutfitoftoday.domain.clothes.entity.QClothes;
import org.example.ootoutfitoftoday.domain.salepost.entity.QSalePost;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void bulkSoftDeleteUserRelatedData(Long id, LocalDateTime deletedAt) {

        QClothes clothes = QClothes.clothes;
        QCloset closet = QCloset.closet;
        QSalePost salePost = QSalePost.salePost;

        // Clothes 논리적 삭제
        jpaQueryFactory.update(clothes)
                .set(clothes.isDeleted, true)
                .set(clothes.deletedAt, deletedAt)
                .where(clothes.user.id.eq(id), clothes.isDeleted.eq(false))
                .execute();

        // Closet 논리적 삭제
        jpaQueryFactory.update(closet)
                .set(closet.isDeleted, true)
                .set(closet.deletedAt, deletedAt)
                .where(closet.user.id.eq(id), clothes.isDeleted.eq(false))
                .execute();

        // SalePost 논리적 삭제
        jpaQueryFactory.update(salePost)
                .set(salePost.isDeleted, true)
                .set(salePost.deletedAt, deletedAt)
                .where(salePost.user.id.eq(id), salePost.isDeleted.eq(false))
                .execute();
    }
}
