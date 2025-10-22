package org.example.ootoutfitoftoday.domain.user.repository;

import java.time.LocalDateTime;

public interface CustomUserRepository {

    void bulkSoftDeleteUserRelatedData(Long id, LocalDateTime deletedAt);
}
