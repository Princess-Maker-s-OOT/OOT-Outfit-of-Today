package org.example.ootoutfitoftoday.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chatparticipatinguser.entity.QChatParticipatingUser;
import org.example.ootoutfitoftoday.domain.closet.entity.QCloset;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.QClosetClothesLink;
import org.example.ootoutfitoftoday.domain.clothes.entity.QClothes;
import org.example.ootoutfitoftoday.domain.salepost.entity.QSalePost;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public void bulkSoftDeleteUserRelatedData(Long id, LocalDateTime deletedAt) {
        QClothes clothes = QClothes.clothes;
        QCloset closet = QCloset.closet;
        QClosetClothesLink closetClothesLink = QClosetClothesLink.closetClothesLink;
        QSalePost salePost = QSalePost.salePost;
        QChatParticipatingUser chatParticipatingUser = QChatParticipatingUser.chatParticipatingUser;

        queryFactory.update(clothes)
                .set(clothes.isDeleted, true)
                .set(clothes.deletedAt, deletedAt)
                .where(clothes.user.id.eq(id), clothes.isDeleted.eq(false))
                .execute();

        queryFactory.update(closet)
                .set(closet.isDeleted, true)
                .set(closet.deletedAt, deletedAt)
                .where(closet.user.id.eq(id), closet.isDeleted.eq(false))
                .execute();

        queryFactory.update(closetClothesLink)
                .set(closetClothesLink.isDeleted, true)
                .set(closetClothesLink.deletedAt, deletedAt)
                .where(closetClothesLink.closet.user.id.eq(id), closetClothesLink.isDeleted.eq(false))
                .execute();

        queryFactory.update(salePost)
                .set(salePost.isDeleted, true)
                .set(salePost.deletedAt, deletedAt)
                .where(salePost.user.id.eq(id), salePost.isDeleted.eq(false))
                .execute();

        queryFactory.update(chatParticipatingUser)
                .set(chatParticipatingUser.isDeleted, true)
                .set(chatParticipatingUser.deletedAt, deletedAt)
                .where(chatParticipatingUser.user.id.eq(id), chatParticipatingUser.isDeleted.eq(false))
                .execute();

        em.clear();
    }
}
