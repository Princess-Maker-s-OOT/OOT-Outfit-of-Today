package org.example.ootoutfitoftoday.domain.transaction.repository;

import jakarta.persistence.LockModeType;
import org.example.ootoutfitoftoday.domain.transaction.entity.Transaction;
import org.example.ootoutfitoftoday.domain.transaction.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findBySalePostIdAndStatusIn(
            Long salePostId,
            List<TransactionStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT t FROM Transaction t
       WHERE t.salePost.id = :salePostId
         AND t.status IN :statuses
    """)
    Optional<Transaction> findActiveBySalePostIdForUpdate(
            @Param("salePostId") Long salePostId,
            @Param("statuses") List<TransactionStatus> statuses
    );
}
