package org.example.ootoutfitoftoday.domain.chat.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.chat.entity.QChat;
import org.example.ootoutfitoftoday.domain.chatroom.entity.QChatroom;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CustomChatRepositoryImpl implements CustomChatRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public void bulkSoftDeleteChatData(Long chatroomId, LocalDateTime deletedAt) {

        QChat chat = QChat.chat;
        QChatroom chatroom = QChatroom.chatroom;

        // Chat 일괄 논리적 삭제
        queryFactory.update(chat)
                .set(chat.isDeleted, true)
                .set(chat.deletedAt, deletedAt)
                .where(chatroom.id.eq(chatroomId), chat.isDeleted.eq(false))
                .execute();

        em.clear();
    }
}
