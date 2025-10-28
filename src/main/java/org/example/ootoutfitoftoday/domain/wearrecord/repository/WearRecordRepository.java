package org.example.ootoutfitoftoday.domain.wearrecord.repository;

import org.example.ootoutfitoftoday.domain.wearrecord.entity.WearRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WearRecordRepository extends JpaRepository<WearRecord, Long> {
}
