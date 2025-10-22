package org.example.ootoutfitoftoday.domain.chat.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chat.entity.QChat;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CustomChatRepositoryImpl implements CustomChatRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public void bulkSoftDeleteChatData(Long id, LocalDateTime deletedAt) {

        QChat chat = QChat.chat;

        // Chat 일괄 논리적 삭제
        queryFactory.update(chat)
                .set(chat.isDeleted, true)
                .set(chat.deletedAt, deletedAt)
                .where(chat.id.eq(id), chat.isDeleted.eq(false))
                .execute();

        em.clear();
    }
}
