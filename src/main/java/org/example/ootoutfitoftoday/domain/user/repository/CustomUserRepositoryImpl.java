package org.example.ootoutfitoftoday.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.closet.entity.QCloset;
import org.example.ootoutfitoftoday.domain.clothes.entity.QClothes;
import org.example.ootoutfitoftoday.domain.salepost.entity.QSalePost;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public void bulkSoftDeleteUserRelatedData(Long id, LocalDateTime deletedAt) {

        QClothes clothes = QClothes.clothes;
        QCloset closet = QCloset.closet;
        QSalePost salePost = QSalePost.salePost;

        // Clothes 논리적 삭제
        queryFactory.update(clothes)
                .set(clothes.isDeleted, true)
                .set(clothes.deletedAt, deletedAt)
                .where(clothes.user.id.eq(id), clothes.isDeleted.eq(false))
                .execute();

        // Closet 논리적 삭제
        queryFactory.update(closet)
                .set(closet.isDeleted, true)
                .set(closet.deletedAt, deletedAt)
                .where(closet.user.id.eq(id), clothes.isDeleted.eq(false))
                .execute();

        // SalePost 논리적 삭제
        queryFactory.update(salePost)
                .set(salePost.isDeleted, true)
                .set(salePost.deletedAt, deletedAt)
                .where(salePost.user.id.eq(id), salePost.isDeleted.eq(false))
                .execute();

        /**
         * 트랜잭션 내에서 벌크 업데이트 후 영속성 컨텍스트 초기화
         * -> 영속성 컨텍스트와 DB 불일치 문제 방지
         * => 총 세 번의 벌크 업데이트가 있으나, 최종 한 번으로 충분
         * */
        em.clear();
    }
}
