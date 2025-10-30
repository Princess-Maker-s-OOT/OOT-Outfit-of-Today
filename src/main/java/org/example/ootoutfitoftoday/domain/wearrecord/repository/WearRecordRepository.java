package org.example.ootoutfitoftoday.domain.wearrecord.repository;

import org.example.ootoutfitoftoday.domain.wearrecord.entity.WearRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WearRecordRepository extends JpaRepository<WearRecord, Long> {

    // 특정 사용자의 착용 기록을 페이징하여 조회
    @Query(value = "SELECT wr FROM WearRecord wr " +
            "JOIN FETCH wr.user u " +
            "JOIN FETCH wr.clothes c " +
            "WHERE u.id = :userId " +
            "ORDER BY wr.wornAt DESC",
            countQuery = "SELECT count(wr) FROM WearRecord wr WHERE wr.user.id = :userId")
    Page<WearRecord> findMyWearRecordsWithClothes(
            @Param("userId") Long userId,
            Pageable pageable
    );
}