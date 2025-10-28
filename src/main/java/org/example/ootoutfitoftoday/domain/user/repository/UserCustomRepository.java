package org.example.ootoutfitoftoday.domain.user.repository;

import java.time.LocalDateTime;

public interface UserCustomRepository {

    void bulkSoftDeleteUserRelatedData(Long id, LocalDateTime deletedAt);
}
