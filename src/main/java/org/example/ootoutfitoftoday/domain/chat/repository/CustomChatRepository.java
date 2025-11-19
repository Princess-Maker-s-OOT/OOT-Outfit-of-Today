package org.example.ootoutfitoftoday.domain.chat.repository;

import java.time.LocalDateTime;

public interface CustomChatRepository {

    void bulkSoftDeleteChatData(Long id, LocalDateTime deletedAt);
}
